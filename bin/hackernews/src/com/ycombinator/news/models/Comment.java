/*
 * Copyright (c) 2024-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package com.ycombinator.news.models;

import org.json.JSONException;
import org.json.JSONObject;

public record Comment(
    boolean isPlaceholder, int id, String by, String text, int[] kids, long time, int depth, boolean collapsed) {
    public static Comment placeholder(int id, int depth) {
        return new Comment(true, id, "", "", new int[0], 0, depth, false);
    }

    public static Comment fromJSON(JSONObject json, int depth) throws JSONException {
        var kidsJson = json.has("kids") ? json.getJSONArray("kids") : null;
        var kids = new int[kidsJson != null ? kidsJson.length() : 0];
        if (kidsJson != null) {
            for (var i = 0; i < kidsJson.length(); i++) {
                kids[i] = kidsJson.getInt(i);
            }
        }
        return new Comment(false, json.getInt("id"), json.optString("by", ""), json.optString("text", ""),
            kids, json.getLong("time"), depth, false);
    }

    public Comment withCollapsed(boolean collapsed) {
        return new Comment(isPlaceholder, id, by, text, kids, time, depth, collapsed);
    }
}
