/*
 * Copyright (c) 2024-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible.models;

import java.io.Serializable;

public record Song(int id, String number, String title) implements Serializable {
}
