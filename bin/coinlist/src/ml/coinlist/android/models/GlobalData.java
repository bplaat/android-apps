/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package ml.coinlist.android.models;

import org.json.JSONException;
import org.json.JSONObject;

public record GlobalData(
    double marketCap, double volume, double marketCapChange, double bitcoinDominance, double ethereumDominance) {
    public static GlobalData fromJSON(JSONObject data, String currencyName) throws JSONException {
        return new GlobalData(data.getJSONObject("total_market_cap").getDouble(currencyName),
            data.getJSONObject("total_volume").getDouble(currencyName),
            data.getDouble("market_cap_change_percentage_24h_usd"),
            data.getJSONObject("market_cap_percentage").getDouble("btc"),
            data.getJSONObject("market_cap_percentage").getDouble("eth"));
    }
}
