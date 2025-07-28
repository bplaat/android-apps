/*
 * Copyright (c) 2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible.models;

public record SongWithText(int id, String number, String title, String text,
        String copyright) {
}
