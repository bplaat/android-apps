/*
 * Copyright (c) 2024-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible.models;

import java.io.Serializable;
import java.util.ArrayList;

public record Book(int id, String key, String name, ArrayList<Chapter> chapters) implements Serializable {
    private static final long serialVersionUID = 1;
}
