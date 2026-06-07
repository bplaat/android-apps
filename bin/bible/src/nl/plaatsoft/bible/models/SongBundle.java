/*
 * Copyright (c) 2024-2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible.models;

import java.util.ArrayList;

public record SongBundle(String path, String name, String abbreviation, String language, String copyright,
    String scrapedAt, ArrayList<SongSection> sections, ArrayList<Song> songs) {}
