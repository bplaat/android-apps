/*
 * Copyright (c) 2025-2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible.models;

public record SongWithText(int id, int sectionId, String number, String title, String text, String copyright) {}
