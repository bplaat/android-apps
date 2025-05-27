/*
 * Copyright (c) 2024-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;

public class Settings {
    private final Context context;
    private final SharedPreferences prefs;

    public Settings(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
    }

    // Language
    public static final int LANGUAGE_ENGLISH = 0;
    public static final int LANGUAGE_DUTCH = 1;
    public static final int LANGUAGE_SYSTEM = 2;
    public static final int LANGUAGE_GERMAN = 3;

    public int getLanguage() {
        return prefs.getInt("language", LANGUAGE_SYSTEM);
    }

    public void setLanguage(int language) {
        if (getLanguage() == language)
            return;
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
        if (getTheme() == theme)
            return;
        prefs.edit().putInt("theme", theme).apply();
    }

    // Font
    public static final int FONT_SERIF = 0;
    public static final int FONT_SANS_SERIF = 1;
    public static final int FONT_MONOSPACE = 2;

    public int getFont() {
        return prefs.getInt("font", FONT_SERIF);
    }

    public Typeface getFontTypeface() {
        if (getFont() == FONT_SANS_SERIF)
            return Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
        if (getFont() == FONT_MONOSPACE)
            return Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
        return Typeface.create(Typeface.SERIF, Typeface.NORMAL);
    }

    public void setFont(int font) {
        if (getFont() == font)
            return;
        prefs.edit().putInt("font", font).apply();
    }

    // Installed assets version
    public String getInstalledAssetsVersion() {
        return prefs.getString("installed_assets_version", "");
    }

    public void setInstalledAssetsVersion(String installedAssetsVersion) {
        if (getInstalledAssetsVersion().equals(installedAssetsVersion))
            return;
        prefs.edit().putString("installed_assets_version", installedAssetsVersion).apply();
    }

    // Open type
    public static final int OPEN_TYPE_BIBLE = 0;
    public static final int OPEN_TYPE_SONG_BUNDLE = 1;

    public int getOpenType() {
        return prefs.getInt("open_type", OPEN_TYPE_BIBLE);
    }

    public void setOpenType(int openType) {
        if (getOpenType() == openType)
            return;
        prefs.edit().putInt("open_type", openType).apply();
    }

    // Open bible
    private String getBibleDefault() {
        var languages = Utils.contextGetLanguages(context);
        for (var language : languages) {
            if (language.equals("nl"))
                return "bibles/nbv21.bible";
            if (language.equals("de"))
                return "bibles/lu17.bible";
        }
        return "bibles/niv.bible";
    }

    public String getOpenBible() {
        return prefs.getString("open_bible", getBibleDefault());
    }

    public void setOpenBible(String openBible) {
        if (getOpenBible().equals(openBible))
            return;
        prefs.edit().putString("open_bible", openBible).apply();
    }

    // Open book
    public String getOpenBook() {
        return prefs.getString("open_book", "GEN");
    }

    public void setOpenBook(String openBook) {
        if (getOpenBook().equals(openBook))
            return;
        prefs.edit().putString("open_book", openBook).apply();
    }

    // Open chapter
    public int getOpenChapter() {
        return prefs.getInt("open_chapter", 1);
    }

    public void setOpenChapter(int openChapterNumber) {
        if (getOpenChapter() == openChapterNumber)
            return;
        prefs.edit().putInt("open_chapter", openChapterNumber).apply();
    }

    // Open chapter scroll
    public int getOpenChapterScroll() {
        return prefs.getInt("open_chapter_scroll", 0);
    }

    public void setOpenChapterScroll(int openChapterScroll) {
        if (getOpenChapterScroll() == openChapterScroll)
            return;
        prefs.edit().putInt("open_chapter_scroll", openChapterScroll).apply();
    }

    // Open song bundle
    public String getOpenSongBundle() {
        return prefs.getString("open_song_bundle", "songbundles/hh.song_bundle");
    }

    public void setOpenSongBundle(String openSongBundle) {
        if (getOpenSongBundle().equals(openSongBundle))
            return;
        prefs.edit().putString("open_song_bundle", openSongBundle).apply();
    }

    // Open song number
    public String getOpenSongNumber() {
        return prefs.getString("open_song_number", "1");
    }

    public void setOpenSongNumber(String openSongBundleNumber) {
        if (getOpenSongNumber().equals(openSongBundleNumber))
            return;
        prefs.edit().putString("open_song_number", openSongBundleNumber).apply();
    }

    // Open song scroll
    public int getOpenSongScroll() {
        return prefs.getInt("open_song_scroll", 0);
    }

    public void setOpenSongScroll(int openSongScroll) {
        if (getOpenSongScroll() == openSongScroll)
            return;
        prefs.edit().putInt("open_song_scroll", openSongScroll).apply();
    }
}
