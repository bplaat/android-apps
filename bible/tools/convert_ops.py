#!/usr/bin/env python3
#
# Copyright (c) 2024 Bastiaan van der Plaat
#
# SPDX-License-Identifier: MIT
#

import argparse
import datetime
import json
import os
import sqlite3

if __name__ == "__main__":
    # Parse arguments
    parser = argparse.ArgumentParser(description="Convert OPS (Opwekking Projectie Systeem 7) database JSON dump")
    parser.add_argument("-n", "--name", help="Name")
    parser.add_argument("-a", "--abbreviation", help="Abbreviation")
    parser.add_argument("-g", "--language", help="Language")
    parser.add_argument("-c", "--copyright", help="Copyright")
    parser.add_argument("-s", "--songs-json", help="Songs JSON table file")
    parser.add_argument("-l", "--lyrics-json", help="Lyrics JSON table file")
    parser.add_argument("-o", "--output", help="Output file")
    args = parser.parse_args()

    # Delete database if exists
    if os.path.exists(args.output):
        os.remove(args.output)

    # Create database
    with sqlite3.connect(args.output) as db:
        print("Converting json to database...")

        # Create tables
        db.execute(
            """
            CREATE TABLE metadata (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                key TEXT UNIQUE,
                value TEXT
            )
        """
        )
        db.execute(
            """
            CREATE TABLE IF NOT EXISTS songs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                number TEXT UNIQUE,
                title TEXT,
                text TEXT,
                copyright TEXT
            )
        """
        )

        # Insert metadata
        db.execute(
            """
            INSERT INTO metadata (key, value) VALUES ("name", ?),
                ("abbreviation", ?),
                ("language", ?),
                ("copyright", ?),
                ("scraped_at", ?)
        """,
            (
                args.name,
                args.abbreviation,
                args.language,
                args.copyright,
                datetime.datetime.now(datetime.timezone.utc).isoformat(),
            ),
        )
        db.commit()

        # Convert songs
        songs = []
        with open(args.songs_json) as f:
            for line in f:
                songs.append(json.loads(line))
        lyrics = []
        with open(args.lyrics_json) as f:
            for line in f:
                lyrics.append(json.loads(line))

        for lyric in lyrics:
            if lyric["Type"] != 0:
                continue
            song = next((song for song in songs if song["ID"] == lyric["SongID"]), None)
            db.execute(
                f"""
                INSERT INTO songs (number, title, text, copyright) VALUES (?, ?, ?, ?)
            """,
                (
                    song["SongNumber"],
                    song["Title"],
                    lyric["Lyrics"].replace("\r\n", "\n"),
                    song["CopyrightText"].replace("\r\n", "\n"),
                ),
            )
        db.commit()

        # Vacuum database
        print("Vacuuming database...")
        db.execute("VACUUM")
        db.commit()

    # Gzip compress database
    print("Compressing database...")
    os.system(f"gzip {args.output}")
    os.rename(f"{args.output}.gz", args.output)
