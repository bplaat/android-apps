package nl.plaatsoft.bassietest;

public class Consts {
    private Consts() {}

    public static final String LOG_TAG = "BassieTest";

    public static final String STORE_PAGE_URL = "https://github.com/bplaat/bassietest-android";

    public static final int ANIMATION_DURATION = 200;
    public static final int ANIMATION_IMAGE_LOADING_TIMEOUT = 50;

    public static final int RATING_ALERT_LAUNCHES_UNTIL_PROMPT = 5;
    public static final int RATING_ALERT_TIME_UNTIL_PROMPT = 2 * 24 * 60 * 60 * 1000;

    public class Settings {
        private Settings() {}

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
