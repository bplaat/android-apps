#!/usr/bin/env python3
#
# Copyright (c) 2024 Bastiaan van der Plaat
#
# SPDX-License-Identifier: MIT
#
# Requires: python3 -m pip install requests bs4

import argparse
from concurrent.futures import ThreadPoolExecutor
import datetime
import os
import sqlite3
import requests
from bs4 import BeautifulSoup

BASE_URL = "https://dagelijksebroodkruimels.nl/songteksten/opwekking/%d"
DOWNLOAD_THREAD_COUNT = 32


def fetch_opwekking_song(song_number):
    # Fetch page
    response = requests.get(BASE_URL % (song_number))
    if response.status_code == 404:
        return None

    # Scrape song title and text
    soup = BeautifulSoup(response.content, "html.parser")

    title_class = "text-2xl font-bold text-center"
    title_text = soup.find("h1", class_=title_class).get_text()

    div_class = "px-2 md:px-0 text-lg leading-8 font-normal text-gray-600 text-center"
    div = soup.find("div", class_=div_class)
    paragraphs = div.find_all("p")
    song_text = "".join(p.get_text() + "\n" for p in paragraphs)

    return (title_text, song_text)


if __name__ == "__main__":
    # Parse arguments
    parser = argparse.ArgumentParser(description="Scrape Opwekking songs")
    parser.add_argument("-n", "--max-number", type=int, help="Max song number")
    parser.add_argument("-o", "--output", help="Output file")
    args = parser.parse_args()

    # Delete database if exists
    if os.path.exists(args.output):
        os.remove(args.output)

    # Create database
    with sqlite3.connect(args.output, check_same_thread=False) as db:
        print("Downloading songs lyrics...")

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
        copyright = "Copyright © %d Stichting Opwekking".format(datetime.datetime.now().year)
        db.execute(
            """
            INSERT INTO metadata (key, value) VALUES ("name", "Opwekking"),
                ("abbreviation", "OPW"),
                ("language", "nl"),
                ("copyright", ?),
                ("scraped_at", ?)
        """,
            (
                copyright,
                datetime.datetime.now(datetime.timezone.utc).isoformat(),
            ),
        )
        db.commit()

        # Scrape songs
        def scrape_and_insert(song_number):
            result = fetch_opwekking_song(song_number)
            if result:
                song_title, song_text = result
                print(f"{song_number}. {song_title}")
                db.execute(
                    """
                    INSERT INTO songs (number, title, text, copyright) VALUES (?, ?, ?, ?)
                    """,
                    (song_number, song_title, song_text, copyright),
                )

        with ThreadPoolExecutor(max_workers=DOWNLOAD_THREAD_COUNT) as executor:
            executor.map(scrape_and_insert, range(1, args.max_number + 1))
        db.commit()

        # Vacuum database
        print("Vacuuming database...")
        db.execute("VACUUM")
        db.commit()

    # Gzip compress database
    print("Compressing database...")
    os.system(f"gzip {args.output}")
    os.rename(f"{args.output}.gz", args.output)
