/*
 * Copyright (c) 2024-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible.models;

import java.io.Serializable;

public record Chapter(int id, int number) implements Serializable {
    private static final long serialVersionUID = 1;
}
