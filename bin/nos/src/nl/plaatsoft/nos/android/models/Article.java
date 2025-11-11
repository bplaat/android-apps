/*
 * Copyright (c) 2019-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.nos.android.models;

import java.io.Serializable;

public record Article(String title, String imageUrl, String date, String content, String externUrl)
    implements Serializable {
    private static final long serialVersionUID = 1;
}
