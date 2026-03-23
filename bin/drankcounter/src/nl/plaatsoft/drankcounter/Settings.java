/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.drankcounter;

import android.app.LocaleManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.LocaleList;

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

    public int getLanguage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            var locales = context.getSystemService(LocaleManager.class).getApplicationLocales();
            if (locales.isEmpty())
                return LANGUAGE_SYSTEM;
            var tag = locales.get(0).getLanguage();
            if (tag.equals("nl"))
                return LANGUAGE_DUTCH;
            return LANGUAGE_ENGLISH;
        }
        return prefs.getInt("language", LANGUAGE_SYSTEM);
    }

    public void setLanguage(int language) {
        if (getLanguage() == language)
            return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            var localeManager = context.getSystemService(LocaleManager.class);
            if (language == LANGUAGE_ENGLISH)
                localeManager.setApplicationLocales(LocaleList.forLanguageTags("en"));
            else if (language == LANGUAGE_DUTCH)
                localeManager.setApplicationLocales(LocaleList.forLanguageTags("nl"));
            else
                localeManager.setApplicationLocales(LocaleList.getEmptyLocaleList());
        } else {
            prefs.edit().putInt("language", language).apply();
        }
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
}
