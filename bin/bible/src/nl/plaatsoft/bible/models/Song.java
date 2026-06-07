/*
 * Copyright (c) 2024-2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible.models;

import java.io.Serializable;

public record Song(int id, int sectionId, String number, String title) implements Serializable {
    private static final long serialVersionUID = 1;
}
