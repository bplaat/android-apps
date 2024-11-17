/*
 * Copyright (c) 2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible.models;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record Song(int id, @Nonnull String number, @Nonnull String title, @Nullable String text,
        @Nullable String copyright) {
}
