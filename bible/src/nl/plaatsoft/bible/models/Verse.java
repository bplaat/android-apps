/*
 * Copyright (c) 2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible.models;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public record Verse(int id, String number, String text, boolean isSubtitle, boolean isLast) {
}
