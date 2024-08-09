package ml.coinlist.android.models;

import android.content.Context;
import android.content.SharedPreferences;
import java.text.NumberFormat;

import ml.coinlist.android.Consts;

public class Coin {
    private final boolean empty;
    private String id;
    private int rank;
    private String name;
    private String imageUrl;
    private double price;
    private double change;
    private double marketcap;
    private double volume;
    private double supply;
    private int extraIndex;
    private boolean starred;

    private Coin() {
        this.empty = true;
    }

    private Coin(
        String id, int rank, String name, String imageUrl, double price,
        double change, double marketcap, double volume, double supply, boolean starred
    ) {
        this.empty = false;
        this.id = id;
        this.rank = rank;
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
        this.change = change;
        this.marketcap = marketcap;
        this.volume = volume;
        this.supply = supply;
        this.extraIndex = 0;
        this.starred = starred;
    }

    public static Coin createEmpty() {
        return new Coin();
    }

    public static Coin createNormal(
        String id, int rank, String name, String imageUrl, double price,
        double change, double marketcap, double volume, double supply, boolean starred
    ) {
        return new Coin(id, rank, name, imageUrl, price, change, marketcap, volume, supply, starred);
    }

    public boolean isEmpty() {
        return empty;
    }

    public String getId() {
        return id;
    }

    public int getRank() {
        return rank;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public double getPrice() {
        return price;
    }

    public double getChange() {
        return change;
    }

    public double getMarketcap() {
        return marketcap;
    }

    public double getVolume() {
        return volume;
    }

    public double getSupply() {
        return supply;
    }

    public int getExtraIndex() {
        return extraIndex;
    }

    public void setExtraIndex(int extraIndex) {
        this.extraIndex = extraIndex;
    }

    public boolean getStarred() {
        return starred;
    }

    public void setStarred(boolean starred) {
        this.starred = starred;
    }

    public static String formatMoney(Context context, double number) {
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
            int decimals = number < 10 ? (number < 0.1 ? 8 : 4) : 2;
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

    public static String formatPercent(double number) {
        var format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(2);
        return format.format(number) + "%";
    }

    public static String formatChangePercent(double number) {
        return (number > 0 ? "\u25b2" : (number < 0 ? "\u25bc" : "")) + formatPercent(number);
    }

    public static String formatNumber(Context context, double number) {
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
