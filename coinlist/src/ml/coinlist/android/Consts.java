package ml.coinlist.android;

public class Consts {
    private Consts() {}

    public static final String STORE_PAGE_URL = "https://github.com/bplaat/android-apps/tree/master/coinlist";

    public class Settings {
        private Settings() {}

        public static final boolean STARRED_ONLY_DEFAULT = false;

        public static final int CURRENCY_USD = 0;
        public static final int CURRENCY_EUR = 1;
        public static final int CURRENCY_BTC = 2;
        public static final int CURRENCY_SATS = 3;
        public static final int CURRENCY_ETH = 4;
        public static final int CURRENCY_BNB = 5;
        public static final int CURRENCY_DEFAULT = Consts.Settings.CURRENCY_USD;
        public static final String[] CURRENCY_NAMES = { "usd", "eur", "btc", "sats", "eth", "bnb" };

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
