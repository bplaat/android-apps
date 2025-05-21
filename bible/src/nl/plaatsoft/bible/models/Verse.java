/*
 * Copyright (c) 2024-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible.models;

public record Verse(int id, String number, String text, boolean isSubtitle, boolean isLast) {
}
