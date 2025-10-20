/*
 * Copyright (c) 2021-2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package ml.coinlist.android.models;

import org.json.JSONException;
import org.json.JSONObject;

public record Coin(boolean isPlaceholder, String id, int rank, String name, String imageUrl, double price,
    double change, double marketCap, double volume, double supply, int visibleStat, boolean starred) {
    public static final int VISIBLE_STAT_MARKET_CAP = 0;
    public static final int VISIBLE_STAT_VOLUME = 1;
    public static final int VISIBLE_STAT_SUPPLY = 2;

    public static final Coin PLACEHOLDER = new Coin(true, "", 0, "", "", 0, 0, 0, 0, 0, VISIBLE_STAT_MARKET_CAP, false);

    public static Coin fromJSON(JSONObject json, boolean isStarred) throws JSONException {
        try {
            return new Coin(false, json.getString("id"), json.getInt("market_cap_rank"), json.getString("name"),
                json.getString("image"), json.getDouble("current_price"), json.getDouble("price_change_percentage_24h"),
                json.getDouble("market_cap"), json.getDouble("total_volume"), json.getDouble("circulating_supply"),
                VISIBLE_STAT_MARKET_CAP, isStarred);
        } catch (JSONException e) {
            return null;
        }
    }

    public Coin nextVisibleStat() {
        return new Coin(isPlaceholder, id, rank, name, imageUrl, price, change, marketCap, volume, supply,
            (visibleStat + 1) % 3, starred);
    }

    public Coin toggleStarred() {
        return new Coin(
            isPlaceholder, id, rank, name, imageUrl, price, change, marketCap, volume, supply, visibleStat, !starred);
    }
}
