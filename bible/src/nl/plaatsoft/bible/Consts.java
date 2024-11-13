/*
 * Copyright (c) 2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible;

import android.content.Context;
import android.graphics.Typeface;

public class Consts {
    private Consts() {
    }

    public static final String STORE_PAGE_URL = "https://github.com/bplaat/android-apps/tree/master/bible";

    public class Settings {
        private Settings() {
        }

        public static final int OPEN_TYPE_BIBLE = 0;
        public static final int OPEN_TYPE_SONG_BUNDLE = 1;
        public static final int OPEN_TYPE_DEFAULT = Consts.Settings.OPEN_TYPE_BIBLE;

        public static String getBibleDefault(Context context) {
            var languages = Utils.contextGetLanguages(context);
            for (var language : languages) {
                if (language.equals("nl"))
                    return "bibles/nbv21.bible";
            }
            return "bibles/niv.bible";
        }

        public static final String BIBLE_BOOK_DEFAULT = "GEN";
        public static final int BIBLE_CHAPTER_DEFAULT = 1;

        public static final String SONG_BUNDLE_DEFAULT = "songbundles/hh.song_bundle";
        public static final String SONG_BUNDLE_NUMBER_DEFAULT = "1";

        public static final int FONT_SERIF = 0;
        public static final int FONT_SANS_SERIF = 1;
        public static final int FONT_MONOSPACE = 2;
        public static final int FONT_DEFAULT = Consts.Settings.FONT_SERIF;

        public static Typeface getFontTypeface(int font) {
            if (font == Consts.Settings.FONT_SANS_SERIF)
                return Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
            if (font == Consts.Settings.FONT_MONOSPACE)
                return Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
            return Typeface.create(Typeface.SERIF, Typeface.NORMAL);
        }

        public static final int LANGUAGE_ENGLISH = 0;
        public static final int LANGUAGE_DUTCH = 1;
        public static final int LANGUAGE_SYSTEM = 2;
        public static final int LANGUAGE_DEFAULT = Consts.Settings.LANGUAGE_SYSTEM;

        public static final int THEME_LIGHT = 0;
        public static final int THEME_DARK = 1;
        public static final int THEME_SYSTEM = 2;
        public static final int THEME_DEFAULT = Consts.Settings.THEME_SYSTEM;

        public static final String ABOUT_WEBSITE_URL = "https://bplaat.nl/";
    }
}
