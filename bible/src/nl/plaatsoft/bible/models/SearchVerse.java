/*
 * Copyright (c) 2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible.models;

public record SearchVerse(
        Verse verse,
        Book book,
        Chapter chapter) {
}
