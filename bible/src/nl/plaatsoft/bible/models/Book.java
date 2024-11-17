/*
 * Copyright (c) 2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible.models;

import java.util.ArrayList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record Book(int id, @Nonnull String key, @Nonnull String name, @Nullable ArrayList<Chapter> chapters) {
}
