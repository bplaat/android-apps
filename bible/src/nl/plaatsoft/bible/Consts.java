package nl.plaatsoft.bible;

import java.util.Map;

public class Consts {
    private Consts() {}

    public static final String STORE_PAGE_URL = "https://github.com/bplaat/android-apps/tree/master/bassietest";

    public class Settings {
        private Settings() {}

        public static final Map<String, String> BIBLE_DEFAULT = Map.of(
            "en", "bibles/niv.bible",
            "nl", "bibles/nbv21.bible"
        );
        public static final String BIBLE_BOOK_DEFAULT = "GEN";
        public static final int BIBLE_CHAPTER_DEFAULT = 1;

        public static final int FONT_SERIF = 0;
        public static final int FONT_SANS_SERIF = 1;
        public static final int FONT_MONOSPACE = 2;
        public static final int FONT_DEFAULT = Consts.Settings.FONT_SERIF;

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
