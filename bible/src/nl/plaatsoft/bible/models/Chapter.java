/*
 * Copyright (c) 2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible.models;

import java.util.ArrayList;
import javax.annotation.Nullable;

public record Chapter(int id, int number, @Nullable ArrayList<Verse> verses) {
}
