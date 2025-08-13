/*
 * Copyright (c) 2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package ml.coinlist.android;

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
        return prefs.getInt("theme", THEME_SYSTEM);
    }

    public void setTheme(int theme) {
        prefs.edit().putInt("theme", theme).apply();
    }

    // Starred only
    public boolean getStarredOnly() {
        return prefs.getBoolean("starred_only", false);
    }

    public void setStarredOnly(boolean starredOnly) {
        prefs.edit().putBoolean("starred_only", starredOnly).apply();
    }

    // Starred coins
    public JSONArray getStarredCoins() {
        try {
            return new JSONArray(prefs.getString("starred_coins", "[]"));
        } catch (JSONException e) {
            return new JSONArray();
        }
    }

    public void setStarredCoins(JSONArray starredCoins) {
        prefs.edit().putString("starred_coins", starredCoins.toString()).apply();
    }

    // Currency
    public static final int CURRENCY_USD = 0;
    public static final int CURRENCY_EUR = 1;
    public static final int CURRENCY_BTC = 2;
    public static final int CURRENCY_SATS = 3;
    public static final int CURRENCY_ETH = 4;
    public static final int CURRENCY_BNB = 5;
    private static final String[] CURRENCY_NAMES = { "usd", "eur", "btc", "sats", "eth", "bnb" };

    public int getCurrency() {
        return prefs.getInt("currency", CURRENCY_USD);
    }

    public String getCurrencyName() {
        return CURRENCY_NAMES[getCurrency()];
    }

    public void setCurrency(int currency) {
        prefs.edit().putInt("currency", currency).apply();
    }

    // Global load time
    public long getGlobalLoadTime() {
        return prefs.getLong("global_load_time", 0);
    }

    public void setGlobalLoadTime(long globalLoadTime) {
        prefs.edit().putLong("global_load_time", globalLoadTime).apply();
    }

    // Coins load time
    public long getCoinsLoadTime() {
        return prefs.getLong("coins_load_time", 0);
    }

    public void setCoinsLoadTime(long coinsLoadTime) {
        prefs.edit().putLong("coins_load_time", coinsLoadTime).apply();
    }
}
