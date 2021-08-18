package ml.coinlist.android;

import java.text.NumberFormat;

public class Coin {
    private String id;
    private int rank;
    private String name;
    private String imageUrl;
    private double price;
    private double marketcap;
    private double volume;
    private double supply;
    private int extraIndex;
    private boolean starred;

    public Coin(String id, int rank, String name, String imageUrl, double price,
        double marketcap, double volume, double supply, boolean starred
    ) {
        this.id = id;
        this.rank = rank;
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
        this.marketcap = marketcap;
        this.volume = volume;
        this.supply = supply;
        this.extraIndex = 0;
        this.starred = starred;
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

    public static String formatMoney(double number) {
        NumberFormat format = NumberFormat.getInstance();
        if (number > 1000000000) {
            format.setMaximumFractionDigits(2);
            return "$" + format.format(number / 1000000000) + " Bn";
        } else if (number > 1000000) {
            format.setMaximumFractionDigits(2);
            return "$" + format.format(number / 1000000) + " M";
        } else {
            format.setMaximumFractionDigits(number < 10 ? (number < 0.1 ? 8 : 4) : 2);
            return "$" + format.format(number);
        }
    }

    public static String formatNumber(double number) {
        NumberFormat format = NumberFormat.getInstance();
        if (number > 1000000000) {
            format.setMaximumFractionDigits(2);
            return format.format(number / 1000000000) + " Bn";
        } else if (number > 1000000) {
            format.setMaximumFractionDigits(2);
            return format.format(number / 1000000) + " M";
        } else {
            format.setMaximumFractionDigits(0);
            return format.format(number);
        }
    }
}
