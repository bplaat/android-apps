/*
 * Copyright (c) 2021-2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package ml.coinlist.android.models;

public class Coin {
    private final boolean empty;
    private String id;
    private int rank;
    private String name;
    private String imageUrl;
    private double price;
    private double change;
    private double marketCap;
    private double volume;
    private double supply;
    private int extraIndex;
    private boolean starred;

    private Coin() {
        this.empty = true;
        this.id = "";
        this.name = "";
        this.imageUrl = "";
    }

    private Coin(
            String id, int rank, String name, String imageUrl, double price,
            double change, double marketCap, double volume, double supply, boolean starred) {
        this.empty = false;
        this.id = id;
        this.rank = rank;
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
        this.change = change;
        this.marketCap = marketCap;
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
            double change, double marketCap, double volume, double supply, boolean starred) {
        return new Coin(id, rank, name, imageUrl, price, change, marketCap, volume, supply, starred);
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

    public double getMarketCap() {
        return marketCap;
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
}
