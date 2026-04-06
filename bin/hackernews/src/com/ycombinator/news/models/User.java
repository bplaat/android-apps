/*
 * Copyright (c) 2024-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package com.ycombinator.news.models;

import org.json.JSONException;
import org.json.JSONObject;
import org.jspecify.annotations.Nullable;

public record User(String id, int karma, long created, @Nullable String about) {
    public static User fromJSON(JSONObject json) throws JSONException {
        return new User(json.getString("id"), json.optInt("karma", 0), json.getLong("created"),
            json.has("about") ? json.getString("about") : null);
    }
}
