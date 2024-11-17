/*
 * Copyright (c) 2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible.models;

import java.util.ArrayList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record Bible(
        @Nonnull String path,
        @Nonnull String name,
        @Nonnull String abbreviation,
        @Nonnull String language,
        @Nonnull String copyright,
        @Nonnull String releasedAt,
        @Nonnull String scrapedAt,
        @Nullable ArrayList<Testament> testaments) {
}
