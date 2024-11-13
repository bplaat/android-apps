/*
 * Copyright (c) 2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

use std::fs::{self, File};
use std::io::{Read, Write};
use std::path::Path;
use std::sync::mpsc;
use std::thread;
use std::time::Duration;

use anyhow::{bail, Context, Result};
use chrono::Datelike;
use clap::Parser;
use flate2::write::GzEncoder;
use flate2::Compression;
use rand::seq::SliceRandom;
use scraper::{Html, Selector};
use threadpool::ThreadPool;

const BASE_URL: &str = "https://dagelijksebroodkruimels.nl/songteksten/opwekking/";
const DOWNLOAD_THREAD_COUNT: usize = 16;

fn fetch_opwekking_song(song_number: i32) -> Result<(String, String)> {
    let url = format!("{}{}", BASE_URL, song_number);
    let body = ureq::get(&url).call()?.into_string()?;
    let document = Html::parse_document(&body);

    let title_selector = Selector::parse("h1.text-2xl.font-bold.text-center").unwrap();
    let title_element = document
        .select(&title_selector)
        .next()
        .context("Can't find title element")?;
    let title_text = title_element.text().collect::<Vec<_>>().concat();

    let div_selector = Selector::parse(
        "div.px-2.md\\:px-0.text-lg.leading-8.font-normal.text-gray-600.text-center",
    )
    .unwrap();
    let div_element = document
        .select(&div_selector)
        .next()
        .context("Can't find song text element")?;
    let paragraph_selector = Selector::parse("p").unwrap();
    let song_text = div_element
        .select(&paragraph_selector)
        .map(|p| p.text().collect::<Vec<_>>().concat() + "\n")
        .collect::<String>();

    Ok((title_text, song_text))
}

#[derive(Parser)]
#[command(version, about = "Download Opwekking songs to .songbundle file")]
struct Args {
    #[arg(default_value = "870")]
    max_number: i32,
    #[arg(short, long, help = "Output file")]
    output: Option<String>,
}

fn main() -> Result<()> {
    // Parse args
    let args = Args::parse();

    // Delete database if exists
    let path = args.output.unwrap_or("opw.songbundle".to_string());
    if Path::new(&path).exists() {
        fs::remove_file(&path)?;
    }

    // Create tables
    let conn = rusqlite::Connection::open(&path)?;
    conn.execute(
        "CREATE TABLE metadata (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            key TEXT UNIQUE,
            value TEXT
        )",
        (),
    )?;
    conn.execute(
        "CREATE TABLE IF NOT EXISTS songs (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            number TEXT UNIQUE,
            title TEXT,
            text TEXT,
            copyright TEXT
        )",
        (),
    )?;

    // Insert metadata
    let copyright = format!(
        "Copyright © {} Stichting Opwekking",
        chrono::Utc::now().year()
    );
    conn.execute(
        "INSERT INTO metadata (key, value) VALUES
            ('name', 'Opwekking'),
            ('abbreviation', 'OPW'),
            ('language', 'nl'),
            ('copyright', ?),
            ('scraped_at', ?)",
        (&copyright, chrono::Utc::now().to_rfc3339()),
    )?;

    // Create fetch song tasks
    let pool = ThreadPool::new(DOWNLOAD_THREAD_COUNT);
    let (tx, rx) = mpsc::channel();
    let mut song_numbers: Vec<i32> = (1..=args.max_number).collect();
    song_numbers.shuffle(&mut rand::thread_rng());
    for number in song_numbers {
        if number == 666 {
            continue;
        }
        let tx = tx.clone();
        pool.execute(move || {
            println!("Downloading Opwekking {}...", number);
            let (title, text) = fetch_opwekking_song(number)
                .unwrap_or_else(|_| panic!("Failed to download Opwekking {}", number));
            tx.send((number, title, text)).unwrap();

            // Slow down fetching because of rate limiting
            thread::sleep(Duration::from_millis(1000));
        });
    }

    // Execute tasks and insert incoming songs until all songs are downloaded
    loop {
        if pool.panic_count() > 0 {
            bail!("Download chapter thread paniced");
        }
        if let Ok(song) = rx.recv_timeout(Duration::from_millis(100)) {
            conn.execute(
                "INSERT INTO songs (number, title, text, copyright) VALUES (?, ?, ?, ?)",
                (song.0, song.1, song.2, &copyright),
            )?;
            continue;
        }
        if (pool.active_count() + pool.queued_count()) == 0 {
            break;
        }
    }

    // Vacuum database
    conn.execute("VACUUM", ())?;
    drop(conn);

    // Gzip compress database
    let mut input_file = std::fs::File::open(&path)?;
    let mut buffer = Vec::new();
    input_file.read_to_end(&mut buffer)?;
    let mut encoder = GzEncoder::new(Vec::new(), Compression::best());
    encoder.write_all(&buffer)?;
    let mut output_file = File::create(path)?;
    output_file.write_all(&encoder.finish()?)?;

    Ok(())
}
