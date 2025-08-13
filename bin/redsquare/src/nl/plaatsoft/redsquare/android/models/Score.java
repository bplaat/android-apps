/*
 * Copyright (c) 2020-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.redsquare.android.models;

import org.json.JSONException;
import org.json.JSONObject;
import org.jspecify.annotations.Nullable;

public record Score(long id, String name, int time, int score, int level) {
    public static @Nullable Score fromJSON(JSONObject json) {
        try {
            return new Score(
                    json.optLong("id", 0),
                    json.getString("name"),
                    json.optInt("time", 0),
                    json.getInt("score"),
                    json.optInt("level", 0));
        } catch (JSONException exception) {
            return null;
        }
    }

    public static @Nullable Score fromPlaatServiceJSON(JSONObject json) {
        try {
            return new Score(
                    json.getLong("sid"),
                    json.optJSONObject("user").getString("nickname"),
                    json.getInt("dt"),
                    json.getInt("score"),
                    json.getInt("level"));
        } catch (JSONException exception) {
            return null;
        }
    }

    public JSONObject toJSON() {
        var json = new JSONObject();
        try {
            json.put("id", id);
            json.put("name", name);
            json.put("time", time);
            json.put("score", score);
            json.put("level", level);
        } catch (JSONException exception) {
        }
        return json;
    }
}
