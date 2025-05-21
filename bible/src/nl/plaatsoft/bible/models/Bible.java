/*
 * Copyright (c) 2024-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible.models;

import java.util.ArrayList;

public record Bible(String path, String name, String abbreviation, String language, String copyright, String releasedAt,
        String scrapedAt, ArrayList<Testament> testaments) {
}
