/*
 * Copyright (c) 2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

use std::fs::{self, File};
use std::io::{BufRead, BufReader, Read, Write};
use std::path::Path;

use anyhow::Result;
use clap::Parser;
use flate2::write::GzEncoder;
use flate2::Compression;
use rand::seq::SliceRandom;
use serde_json::Value;

#[derive(Parser)]
#[command(
    version,
    about = "A tool to convert OPS (Opwekking Projectie Systeem 7) database JSON dump to .songbundle file"
)]
struct Args {
    #[arg(long)]
    name: String,
    #[arg(long)]
    abbreviation: String,
    #[arg(long)]
    language: String,
    #[clap(long)]
    copyright: String,
    #[arg(name = "songs", long, help = "Songs JSON table file")]
    songs_json: String,
    #[arg(name = "lyrics", long, help = "Lyrics JSON table file")]
    lyrics_json: String,
    #[arg(short, long, help = "Output file")]
    output: Option<String>,
}

fn main() -> Result<()> {
    // Parse args
    let args = Args::parse();

    // Delete database if exists
    let path = args
        .output
        .unwrap_or(format!("{}.songbundle", args.abbreviation));
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
    conn.execute(
        "INSERT INTO metadata (key, value) VALUES (?, ?), (?, ?), (?, ?), (?, ?), (?, ?)",
        (
            "name",
            args.name,
            "abbreviation",
            args.abbreviation.to_uppercase(),
            "language",
            args.language,
            "copyright",
            args.copyright,
            "scraped_at",
            chrono::Utc::now().to_rfc3339(),
        ),
    )?;

    // Convert songs
    let songs_file = File::open(args.songs_json)?;
    let songs_reader = BufReader::new(songs_file);
    let mut songs: Vec<Value> = Vec::new();
    for line in songs_reader.lines() {
        songs.push(serde_json::from_str(&line?)?);
    }

    let lyrics_file = File::open(args.lyrics_json)?;
    let lyrics_reader = BufReader::new(lyrics_file);
    let mut lyrics: Vec<Value> = Vec::new();
    for line in lyrics_reader.lines() {
        lyrics.push(serde_json::from_str(&line?)?);
    }
    lyrics.shuffle(&mut rand::thread_rng());

    for lyric in &lyrics {
        if lyric["Type"].as_i64().unwrap() != 0 {
            continue;
        }

        let song = songs
            .iter()
            .find(|&song| song["ID"] == lyric["SongID"])
            .unwrap();
        conn.execute(
            "INSERT INTO songs (number, title, text, copyright) VALUES (?, ?, ?, ?)",
            (
                song["SongNumber"].as_number().unwrap().to_string(),
                song["Title"].as_str().unwrap(),
                lyric["Lyrics"].as_str().unwrap().replace("\r\n", "\n"),
                song["CopyrightText"]
                    .as_str()
                    .unwrap()
                    .replace("\r\n", "\n"),
            ),
        )?;
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
