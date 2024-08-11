use std::collections::HashMap;
use std::fs::File;
use std::io::{Read, Write};
use std::sync::mpsc;
use std::time::Duration;

use anyhow::{anyhow, bail, Result};
use clap::Parser;
use flate2::write::GzEncoder;
use flate2::Compression;
use serde_json::Value;
use threadpool::ThreadPool;

use crate::structs::{ChapterContents, Metadata};

mod structs;

fn fetch_bible_chapter(url: &str) -> Result<(ChapterContents, Metadata)> {
    let body: String = ureq::get(url).call()?.into_string()?;
    let marker = "<script id=\"IBEP-main-state\" type=\"application/json\">";
    let data_str = &body[body.find(marker).unwrap() + marker.len()..];
    let data_value =
        serde_json::from_str::<Value>(&data_str[..data_str.find("</script>").unwrap()])?;

    let mut metadata = None;
    let mut chapter_contents = None;
    for (url, value) in data_value.as_object().unwrap() {
        if url.contains("/metadata") {
            metadata = Some(serde_json::from_value::<structs::Metadata>(
                value.get("data").unwrap().clone(),
            )?);
        }
        if url.contains("/chapters") {
            chapter_contents = Some(serde_json::from_value::<structs::ChapterContents>(
                value.get("data").unwrap().get("chapter").unwrap().clone(),
            )?);
        }
    }
    Ok((chapter_contents.unwrap(), metadata.unwrap()))
}

struct Chapter {
    id: i64,
    book_key: String,
    number: i32,
}

struct Verse {
    chapter_id: i64,
    number: Option<String>,
    text: String,
    is_subtitle: bool,
    is_new_paragraph: bool,
}

#[derive(Parser, Debug)]
#[command(version, about, long_about = None)]
struct Args {
    #[arg()]
    translation: String,
    #[arg(short, long)]
    output: Option<String>,
}

fn main() -> Result<()> {
    // Parse args
    let args = Args::parse();

    // Create tables
    let path = args.output.unwrap_or(format!("{}.bible", args.translation));
    let conn = rusqlite::Connection::open(&path)?;
    conn.execute(
        r"CREATE TABLE metadata (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            key TEXT UNIQUE,
            value TEXT
        )",
        (),
    )?;
    conn.execute(
        r"CREATE TABLE testaments (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                key TEXT UNIQUE,
                name TEXT
            )",
        (),
    )?;
    conn.execute(
        r"CREATE TABLE books (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            testament_id INT,
            key TEXT UNIQUE,
            name TEXT,
            FOREIGN KEY (testament_id) REFERENCES testaments (id)
        )",
        (),
    )?;
    conn.execute(
        r"CREATE TABLE chapters (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            book_id INT,
            number INT,
            FOREIGN KEY (book_id) REFERENCES books (id)
        )",
        (),
    )?;
    conn.execute(
        r"CREATE TABLE verses (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            chapter_id INT,
            number TEXT,
            text TEXT,
            is_subtitle BOOL,
            is_new_paragraph BOOL,
            FOREIGN KEY (chapter_id) REFERENCES chapters (id)
        )",
        (),
    )?;

    // Fetch metadata
    let source_urls = vec![
        "https://www.debijbel.nl/bijbel",
        "https://www.die-bibel.de/bibel",
    ];
    let translation_upper = args.translation.to_uppercase();
    for source_url in source_urls {
        println!(
            "Fetching {} metadata from {}...",
            translation_upper, source_url
        );
        let (_, translation_metadata) =
            match fetch_bible_chapter(&format!("{}/{}/GEN.1", source_url, translation_upper)) {
                Ok(data) => data,
                Err(_) => continue,
            };
        if translation_upper != translation_metadata.abbreviation {
            continue;
        }

        // Write metadata
        let mut metadata = HashMap::new();
        metadata.insert("name", translation_metadata.name_local);
        metadata.insert("abbreviation", translation_metadata.abbreviation_local);
        metadata.insert("language", translation_metadata.language.name_local); // FIXME
        metadata.insert("copyright", translation_metadata.copyright);
        metadata.insert("released_at", translation_metadata.updated_at);
        for (key, value) in metadata {
            conn.execute(
                "INSERT INTO metadata (key, value) VALUES (?, ?)",
                (key, value),
            )?;
        }

        // Write testaments, books, and chapters
        let mut chapters = Vec::new();
        for testament in &translation_metadata.testaments {
            let name = match translation_metadata.language.id.as_str() {
                "nld" => match testament.abbreviation.as_str() {
                    "OT" => "Oude Testament",
                    "NT" => "Nieuwe Testament",
                    "DC" => "Deuterocanonieke boeken",
                    _ => todo!(),
                },
                "eng" => match testament.abbreviation.as_str() {
                    "OT" => "Old Testament",
                    "NT" => "New Testament",
                    "DC" => "Deuterocanonical books",
                    _ => todo!(),
                },
                _ => todo!(),
            };
            conn.execute(
                "INSERT INTO testaments (key, name) VALUES (?, ?)",
                (&testament.abbreviation, name),
            )?;
            let testament_id = conn.last_insert_rowid();

            for book in &testament.books {
                conn.execute(
                    "INSERT INTO books (testament_id, key, name) VALUES (?, ?, ?)",
                    (testament_id, &book.id, &book.name),
                )?;
                let book_id = conn.last_insert_rowid();

                for chapter in &book.chapters {
                    let chapter_number = chapter.number.parse::<i32>()?;
                    conn.execute(
                        "INSERT INTO chapters (book_id, number) VALUES (?, ?)",
                        (book_id, chapter_number),
                    )?;
                    if book.id == "GEN" {
                        // FIXME
                        chapters.push(Chapter {
                            id: conn.last_insert_rowid(),
                            book_key: book.id.clone(),
                            number: chapter_number,
                        });
                    }
                }
            }
        }

        // Queue download chapters tasks
        let pool = ThreadPool::new(8); // FIXME
        let (tx, rx) = mpsc::channel();
        for chapter in chapters {
            let translation_upper = translation_upper.clone();
            let tx = tx.clone();
            pool.execute(move || {
                println!("Downloading {}.{}...", chapter.book_key, chapter.number);
                let (chapter_data, _) = fetch_bible_chapter(&format!(
                    "{}/{}/{}.{}",
                    source_url, translation_upper, chapter.book_key, chapter.number
                ))
                .unwrap();

                let mut verses: Vec<Verse> = Vec::new();
                let mut current_verse_number = None;
                let mut current_verse_text = String::new();
                let mut is_new_paragraph = true;
                for block in &chapter_data.blocks {
                    match block.r#type.as_str() {
                        "paragraph" => {
                            if let Some(content) = &block.content {
                                for (i, item) in content.iter().enumerate() {
                                    match item.r#type.as_str() {
                                        "text" => {
                                            if current_verse_number.is_some() {
                                                verses.push(Verse {
                                                    chapter_id: chapter.id,
                                                    number: current_verse_number.clone(),
                                                    text: current_verse_text.clone(),
                                                    is_subtitle: false,
                                                    is_new_paragraph,
                                                });
                                                current_verse_number = None;
                                                current_verse_text.clear();
                                                is_new_paragraph = i == 0;
                                            }

                                            verses.push(Verse {
                                                chapter_id: chapter.id,
                                                number: None,
                                                text: item.content.clone().unwrap(),
                                                is_subtitle: true,
                                                is_new_paragraph: true,
                                            });
                                        }
                                        "verse-number" => {
                                            if current_verse_number.is_some() {
                                                verses.push(Verse {
                                                    chapter_id: chapter.id,
                                                    number: current_verse_number.clone(),
                                                    text: current_verse_text.clone(),
                                                    is_subtitle: false,
                                                    is_new_paragraph,
                                                });
                                                current_verse_text.clear();
                                                is_new_paragraph = i == 0;
                                            }

                                            current_verse_number =
                                                Some(item.content.clone().unwrap());
                                        }
                                        "verse-text" | "char" => {
                                            if let Some(text) = &item.content {
                                                if text != " " {
                                                    current_verse_text.push_str(text.as_str());
                                                }
                                            }
                                        }
                                        "study" => {}
                                        _ => todo!(),
                                    }
                                }
                                if current_verse_number.is_some() {
                                    current_verse_text.push('\n');
                                }
                            }
                        }
                        _ => todo!(),
                    }
                }
                if current_verse_number.is_some() {
                    verses.push(Verse {
                        chapter_id: chapter.id,
                        number: current_verse_number.clone(),
                        text: current_verse_text.clone(),
                        is_subtitle: false,
                        is_new_paragraph,
                    });
                }

                tx.send(verses).unwrap();
            });
        }

        // Execute tasks and insert incoming verses until all chapters are downloaded
        loop {
            if pool.panic_count() > 0 {
                bail!("Download chapter thread paniced");
            }
            if let Ok(verses) = rx.recv_timeout(Duration::from_millis(100)) {
                for verse in &verses {
                    conn.execute(
                        "INSERT INTO verses (chapter_id, number, text, is_subtitle, is_new_paragraph) VALUES (?, ?, ?, ?, ?)",
                        (verse.chapter_id, &verse.number, &verse.text, verse.is_subtitle, verse.is_new_paragraph),
                    )?;
                }
                continue;
            }
            if (pool.active_count() + pool.queued_count()) == 0 {
                break;
            }
        }

        // Vacuum database
        conn.execute("VACUUM", ())?;
        conn.close().map_err(|_| anyhow!("Can't close database"))?;

        // Gzip compress database
        println!("Compressing {}...", path);
        let mut input_file = std::fs::File::open(&path)?;
        let mut buffer = Vec::new();
        input_file.read_to_end(&mut buffer)?;
        let mut encoder = GzEncoder::new(Vec::new(), Compression::best());
        encoder.write_all(&buffer)?;
        let mut output_file = File::create(path)?;
        output_file.write_all(&encoder.finish()?)?;

        return Ok(());
    }
    bail!("Can't find translation")
}