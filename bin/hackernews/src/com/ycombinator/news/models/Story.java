/*
 * Copyright (c) 2024-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package com.ycombinator.news.models;

import org.json.JSONException;
import org.json.JSONObject;
import org.jspecify.annotations.Nullable;

public record Story(boolean isPlaceholder, boolean isLoadMore, int id, int rank, String title, @Nullable String url,
    String by, int score, long time, int descendants, int[] kids, String type, @Nullable String text) {
    public static Story placeholder(int rank) {
        return new Story(true, false, 0, rank, "", null, "", 0, 0, 0, new int[0], "story", null);
    }

    public static final Story LOAD_MORE = new Story(false, true, -1, 0, "", null, "", 0, 0, 0, new int[0], "", null);

    public static Story fromJSON(JSONObject json, int rank) throws JSONException {
        var url = json.has("url") ? json.getString("url") : null;
        var text = json.has("text") ? json.getString("text") : null;
        var kidsJson = json.has("kids") ? json.getJSONArray("kids") : null;
        var kids = new int[kidsJson != null ? kidsJson.length() : 0];
        if (kidsJson != null) {
            for (var i = 0; i < kidsJson.length(); i++) {
                kids[i] = kidsJson.getInt(i);
            }
        }
        return new Story(false, false, json.getInt("id"), rank, json.optString("title", ""), url,
            json.optString("by", ""), json.optInt("score", 0), json.getLong("time"), json.optInt("descendants", 0),
            kids, json.optString("type", "story"), text);
    }

    public @Nullable String domain() {
        if (url == null)
            return null;
        try {
            var host = new java.net.URI(url).getHost();
            if (host == null)
                return null;
            return host.startsWith("www.") ? host.substring(4) : host;
        } catch (Exception e) {
            return null;
        }
    }
}
