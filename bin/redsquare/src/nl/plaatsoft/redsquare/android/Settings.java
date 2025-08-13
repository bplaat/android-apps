/*
 * Copyright (c) 2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.redsquare.android;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

public class Settings {
    private final SharedPreferences prefs;

    public Settings(Context context) {
        prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
    }

    // Language
    public static final int LANGUAGE_ENGLISH = 0;
    public static final int LANGUAGE_DUTCH = 1;
    public static final int LANGUAGE_SYSTEM = 2;

    public int getLanguage() {
        return prefs.getInt("language", LANGUAGE_SYSTEM);
    }

    public void setLanguage(int language) {
        prefs.edit().putInt("language", language).apply();
    }

    // Theme
    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_SYSTEM = 2;

    public int getTheme() {
        return prefs.getInt("theme", THEME_DARK);
    }

    public void setTheme(int theme) {
        prefs.edit().putInt("theme", theme).apply();
    }

    // Name
    public String getName() {
        return prefs.getString("name", "Anonymous");
    }

    public void setName(String name) {
        prefs.edit().putString("name", name).apply();
    }

    // Score
    public JSONArray getScores() {
        try {
            return new JSONArray(prefs.getString("scores", "[]"));
        } catch (JSONException e) {
            return new JSONArray();
        }
    }

    public void setScores(JSONArray score) {
        prefs.edit().putString("scores", score.toString()).apply();
    }
}
