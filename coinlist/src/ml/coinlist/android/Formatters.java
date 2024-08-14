/*
 * Copyright (c) 2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package ml.coinlist.android;

import android.content.Context;
import android.content.SharedPreferences;
import java.text.NumberFormat;

import ml.coinlist.android.Consts;

public class Formatters {
    private Formatters() {}

    public static String money(Context context, double number) {
        var settings = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        var currency = settings.getInt("currency", Consts.Settings.CURRENCY_DEFAULT);

        var format = NumberFormat.getInstance();
        String formatted;
        if (number > 1e12 && currency == Consts.Settings.CURRENCY_SATS) {
            format.setMaximumFractionDigits(2);
            format.setMinimumFractionDigits(2);
            formatted = format.format(number / 1e12) + " T";
        } else if (number > 1e9) {
            format.setMaximumFractionDigits(2);
            format.setMinimumFractionDigits(2);
            formatted = format.format(number / 1e9) + " Bn";
        } else if (number > 1e6) {
            format.setMaximumFractionDigits(2);
            format.setMinimumFractionDigits(2);
            formatted = format.format(number / 1e6) + " M";
        } else {
            var decimals = number < 10 ? (number < 0.1 ? 8 : 4) : 2;
            if (currency == Consts.Settings.CURRENCY_BTC || currency == Consts.Settings.CURRENCY_ETH || currency == Consts.Settings.CURRENCY_BNB)
                decimals = number < 10 ? (number < 0.1 ? 12 : 6) : 4;
            if (currency == Consts.Settings.CURRENCY_SATS)
                decimals = number < 1 ? 4 : 0;
            format.setMaximumFractionDigits(decimals);
            format.setMinimumFractionDigits(decimals);
            formatted = format.format(number);
        }

        if (currency == Consts.Settings.CURRENCY_EUR)
            return "\u20ac" + formatted;
        if (currency == Consts.Settings.CURRENCY_BTC)
            return "\uu20bf" + formatted;
        if (currency == Consts.Settings.CURRENCY_SATS)
            return formatted + " SATS";
        if (currency == Consts.Settings.CURRENCY_ETH)
            return "\u039e" + formatted;
        if (currency == Consts.Settings.CURRENCY_BNB)
            return formatted + " BNB";
        return "$" + formatted;
    }

    public static String percent(double number) {
        var format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(2);
        return format.format(number) + "%";
    }

    public static String changePercent(double number) {
        return (number > 0 ? "\u25b2" : (number < 0 ? "\u25bc" : "")) + percent(number);
    }

    public static String number(Context context, double number) {
        var settings = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        var currency = settings.getInt("currency", Consts.Settings.CURRENCY_DEFAULT);
        var format = NumberFormat.getInstance();
        if (number > 1e12 && currency == Consts.Settings.CURRENCY_SATS) {
            format.setMaximumFractionDigits(2);
            format.setMinimumFractionDigits(2);
            return format.format(number / 1e12) + " T";
        }
        if (number > 1e9) {
            format.setMaximumFractionDigits(2);
            format.setMinimumFractionDigits(2);
            return format.format(number / 1e9) + " Bn";
        }
        if (number > 1e6) {
            format.setMaximumFractionDigits(2);
            format.setMinimumFractionDigits(2);
            return format.format(number / 1e6) + " M";
        }
        format.setMaximumFractionDigits(0);
        return format.format(number);
    }
}
