/*
 * Copyright (c) 2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible.models;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public record SearchVerse(
        Verse verse,
        Book book,
        Chapter chapter) {
}
