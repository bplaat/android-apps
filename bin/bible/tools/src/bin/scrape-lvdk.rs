/*
 * Copyright (c) 2024-2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

use std::collections::HashSet;
use std::fs::{self, File};
use std::io::{Read, Write};
use std::path::Path;
use std::sync::mpsc;
use std::thread;
use std::time::Duration;

use anyhow::{bail, Context, Result};
use clap::Parser;
use flate2::write::GzEncoder;
use flate2::Compression;
use rand::seq::SliceRandom;
use scraper::{Html, Selector};
use threadpool::ThreadPool;

const INDEX_URL: &str = "https://kerkliedwiki.nl/Liedboek_voor_de_kerken/Inhoud";
const LYRICS_BASE_URL: &str = "https://psalmengezang.wordpress.com";
const DOWNLOAD_THREAD_COUNT: usize = 8;

#[derive(Debug, Clone)]
struct Song {
    number: String,
    title: String,
    text: String,
    section_id: i64,
}

fn scrape_index() -> Result<(Vec<(i32, String)>, Vec<(String, String)>)> {
    println!("Scraping index from kerkliedwiki.nl...");
    let body = ureq::get(INDEX_URL).call()?.into_string()?;
    let document = Html::parse_document(&body);

    let li_selector = Selector::parse("#mw-content-text li").unwrap();
    let a_selector = Selector::parse("a").unwrap();

    let mut psalms: Vec<(i32, String)> = Vec::new();
    let mut gezangen: Vec<(String, String)> = Vec::new();
    let mut seen_psalm_numbers: HashSet<i32> = HashSet::new();

    for li in document.select(&li_selector) {
        // Skip TOC entries (they have toclevel-* class)
        if let Some(class) = li.value().attr("class") {
            if class.contains("toclevel") {
                continue;
            }
        }

        let text = li.text().collect::<Vec<_>>().concat();
        let text = text.trim().to_string();

        // Detect psalm entries: "NUMBER TITLE" with a link to the song page
        if let Some(a) = li.select(&a_selector).next() {
            let href = a.value().attr("href").unwrap_or("");
            // Psalm links go to song pages (start with /), not section anchors (#)
            if !href.starts_with('#') && text.starts_with(|c: char| c.is_ascii_digit()) {
                if let Some(space_pos) = text.find(' ') {
                    let number_str = &text[..space_pos];
                    if let Ok(number) = number_str.parse::<i32>() {
                        if number >= 1 && number <= 150 && !seen_psalm_numbers.contains(&number) {
                            // Get title from the link text
                            let title = a.text().collect::<Vec<_>>().concat().trim().to_string();
                            seen_psalm_numbers.insert(number);
                            psalms.push((number, title));
                            continue;
                        }
                    }
                }
            }
        }

        // Detect gezang entries: "TITLE (NUMBER)" at end
        if let Some(paren_start) = text.rfind('(') {
            if let Some(paren_end) = text.rfind(')') {
                let number_str = text[paren_start + 1..paren_end].trim();
                let title = text[..paren_start].trim().to_string();

                if !title.is_empty() && !number_str.is_empty() {
                    gezangen.push((number_str.to_string(), title));
                }
            }
        }
    }

    println!(
        "Found {} psalms and {} gezangen in index",
        psalms.len(),
        gezangen.len()
    );
    Ok((psalms, gezangen))
}

fn fetch_lyrics(url: &str) -> Result<String> {
    let body = ureq::get(url).call()?.into_string()?;
    let document = Html::parse_document(&body);

    let content_selector = Selector::parse(".entry-content").unwrap();
    let content = document
        .select(&content_selector)
        .next()
        .context("Can't find entry-content")?;

    let table_selector = Selector::parse("table").unwrap();
    let td_selector = Selector::parse("td").unwrap();

    let mut verses: Vec<String> = Vec::new();

    for table in content.select(&table_selector) {
        let mut verse_lines: Vec<String> = Vec::new();
        let tds: Vec<_> = table.select(&td_selector).collect();

        // Each table row has 2 tds: [verse_number_or_empty, lyric_line]
        // Process in pairs
        let mut i = 0;
        while i + 1 < tds.len() {
            let first_td = tds[i]
                .text()
                .collect::<Vec<_>>()
                .concat()
                .trim()
                .to_string();
            let second_td = tds[i + 1]
                .text()
                .collect::<Vec<_>>()
                .concat()
                .trim()
                .to_string();

            // First line of verse has number in first td
            if !first_td.is_empty() && !verse_lines.is_empty() {
                // Start of new verse within same table (shouldn't happen normally)
                verses.push(verse_lines.join("\n"));
                verse_lines.clear();
            }

            if !first_td.is_empty() {
                verse_lines.push(format!("{} {}", first_td, second_td));
            } else if !second_td.is_empty() {
                verse_lines.push(second_td);
            }

            i += 2;
        }

        if !verse_lines.is_empty() {
            verses.push(verse_lines.join("\n"));
        }
    }

    Ok(verses.join("\n\n") + "\n")
}

#[derive(Parser)]
#[command(
    version,
    about = "Scrape Liedboek voor de Kerken songs to .songbundle file"
)]
struct Args {
    #[arg(short, long, default_value = "lvdk.songbundle")]
    output: String,
}

fn main() -> Result<()> {
    let args = Args::parse();

    // Scrape index
    let (psalms, gezangen) = scrape_index()?;

    // Delete database if exists
    if Path::new(&args.output).exists() {
        fs::remove_file(&args.output)?;
    }

    // Create tables
    let conn = rusqlite::Connection::open(&args.output)?;
    conn.execute(
        "CREATE TABLE metadata (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            key TEXT UNIQUE,
            value TEXT
        )",
        (),
    )?;
    conn.execute(
        "CREATE TABLE sections (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            singular_name TEXT NOT NULL
        )",
        (),
    )?;
    conn.execute(
        "CREATE TABLE songs (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            section_id INTEGER,
            number TEXT,
            title TEXT,
            text TEXT,
            copyright TEXT,
            UNIQUE(section_id, number),
            FOREIGN KEY (section_id) REFERENCES sections (id)
        )",
        (),
    )?;

    // Insert metadata
    let copyright = "Copyright \u{00a9} 1973 Interkerkelijke Stichting voor het Kerklied";
    conn.execute(
        "INSERT INTO metadata (key, value) VALUES
            ('name', 'Liedboek voor de Kerken'),
            ('abbreviation', 'LvdK'),
            ('language', 'nl'),
            ('copyright', ?),
            ('scraped_at', ?)",
        (copyright, chrono::Utc::now().to_rfc3339()),
    )?;

    // Insert sections
    conn.execute(
        "INSERT INTO sections (name, singular_name) VALUES ('Psalmen', 'Psalm'), ('Gezangen', 'Gezang')",
        (),
    )?;
    let psalmen_section_id: i64 = conn.query_row(
        "SELECT id FROM sections WHERE name = 'Psalmen'",
        (),
        |row| row.get(0),
    )?;
    let gezangen_section_id: i64 = conn.query_row(
        "SELECT id FROM sections WHERE name = 'Gezangen'",
        (),
        |row| row.get(0),
    )?;

    // Build download tasks: psalms + gezangen
    #[derive(Clone)]
    struct DownloadTask {
        number: String,
        title: String,
        url: String,
        section_id: i64,
    }

    let mut tasks: Vec<DownloadTask> = Vec::new();

    for (num, title) in &psalms {
        tasks.push(DownloadTask {
            number: num.to_string(),
            title: title.clone(),
            url: format!("{}/ps/psalm-{num}/", LYRICS_BASE_URL),
            section_id: psalmen_section_id,
        });
    }

    // Track gezang numbers already added to avoid duplicates
    let mut seen_gezang_numbers: HashSet<String> = HashSet::new();
    for (gz_number, title) in &gezangen {
        let number = gz_number.trim().to_string();
        if number.is_empty() || seen_gezang_numbers.contains(&number) {
            continue;
        }

        // Extract base number for URL (first numeric part)
        let url_number = number
            .chars()
            .take_while(|c| c.is_ascii_digit())
            .collect::<String>();
        if url_number.is_empty() {
            continue;
        }

        seen_gezang_numbers.insert(number.clone());
        tasks.push(DownloadTask {
            number: number.clone(),
            title: title.clone(),
            url: format!("{}/gz/gezang-{url_number}/", LYRICS_BASE_URL),
            section_id: gezangen_section_id,
        });
    }

    println!("Downloading {} songs...", tasks.len());

    // Create fetch tasks
    let pool = ThreadPool::new(DOWNLOAD_THREAD_COUNT);
    let (tx, rx) = mpsc::channel();
    tasks.shuffle(&mut rand::thread_rng());

    for task in tasks {
        let tx = tx.clone();
        pool.execute(move || {
            println!("Downloading {}...", task.number);
            match fetch_lyrics(&task.url) {
                Ok(text) => {
                    tx.send(Some(Song {
                        number: task.number,
                        title: task.title,
                        text,
                        section_id: task.section_id,
                    }))
                    .unwrap();
                }
                Err(e) => {
                    eprintln!("Warning: Failed to download {}: {}", task.number, e);
                    tx.send(None).unwrap();
                }
            }

            // Rate limiting
            thread::sleep(Duration::from_millis(1000));
        });
    }

    // Receive and insert songs
    let mut count = 0;
    loop {
        if pool.panic_count() > 0 {
            bail!("Download thread panicked");
        }
        if let Ok(song) = rx.recv_timeout(Duration::from_millis(100)) {
            if let Some(song) = song {
                conn.execute(
                    "INSERT INTO songs (section_id, number, title, text, copyright) VALUES (?, ?, ?, ?, ?)",
                    (&song.section_id, &song.number, &song.title, &song.text, copyright),
                )?;
                count += 1;
            }
            continue;
        }
        if (pool.active_count() + pool.queued_count()) == 0 {
            break;
        }
    }

    println!("Inserted {} songs", count);

    // Vacuum database
    conn.execute("VACUUM", ())?;
    drop(conn);

    // Gzip compress database
    let mut input_file = File::open(&args.output)?;
    let mut buffer = Vec::new();
    input_file.read_to_end(&mut buffer)?;
    let mut encoder = GzEncoder::new(Vec::new(), Compression::best());
    encoder.write_all(&buffer)?;
    let mut output_file = File::create(&args.output)?;
    output_file.write_all(&encoder.finish()?)?;

    println!("Done! Output: {}", args.output);
    Ok(())
}
