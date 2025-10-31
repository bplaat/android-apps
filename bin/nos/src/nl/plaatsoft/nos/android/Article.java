/*
 * Copyright (c) 2019-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.nos.android;

import java.io.Serializable;

public class Article implements Serializable {
    private static final long serialVersionUID = 1;

    private final String title;
    private final String imageUrl;
    private final String date;
    private final String content;

    public Article(String title, String imageUrl, String date, String content) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.date = date;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDate() {
        return date;
    }

    public String getContent() {
        return content;
    }
}
