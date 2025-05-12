/*
 * Copyright (c) 2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bassiemusic;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {
    private final SharedPreferences prefs;

    public Settings(Context context) {
        prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
    }

    // Remember music
    public boolean isRememberMusic() {
        return prefs.getBoolean("remember_music", true);
    }

    public void setRememberMusic(boolean rememberMusic) {
        var prefsEditor = prefs.edit();
        prefsEditor.putBoolean("remember_music", rememberMusic);
        if (!rememberMusic) {
            prefsEditor.remove("playing_music_id");
            prefsEditor.remove("playing_music_position");
        }
        prefsEditor.apply();
    }

    // Fast scroll
    public boolean isFastScroll() {
        return prefs.getBoolean("fast_scroll", true);
    }

    public void setFastScroll(boolean fastScroll) {
        prefs.edit().putBoolean("fast_scroll", fastScroll).apply();
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
        return prefs.getInt("theme", THEME_SYSTEM);
    }

    public void setTheme(int theme) {
        prefs.edit().putInt("theme", theme).apply();
    }

    // Shuffling
    public boolean isShuffling() {
        return prefs.getBoolean("shuffling", false);
    }

    public void setShuffling(boolean shuffling) {
        prefs.edit().putBoolean("shuffling", shuffling).apply();
    }

    // Playing music ID
    public long getPlayingMusicId() {
        return prefs.getLong("playing_music_id", -1);
    }

    public void setPlayingMusicId(long playingMusicId) {
        prefs.edit().putLong("playing_music_id", playingMusicId).apply();
    }

    // Playing music position
    public int getPlayingMusicPosition() {
        return prefs.getInt("playing_music_position", 0);
    }

    public void setPlayingMusicPosition(int playingMusicPosition) {
        prefs.edit().putInt("playing_music_position", playingMusicPosition).apply();
    }
}
