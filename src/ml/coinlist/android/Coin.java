package ml.coinlist.android;

public class Coin {
    private String id;
    private int rank;
    private String name;
    private String imageUrl;
    private double price;
    private boolean starred;

    public Coin(String id, int rank, String name, String imageUrl, double price, boolean starred) {
        this.id = id;
        this.rank = rank;
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
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

    public String getPriceFormatted() {
        int decimals = price < 10 ? (price < 0.1 ? 8 : 4) : 2;
        return String.format("$ %." + decimals + "f", price);
    }

    public boolean getStarred() {
        return starred;
    }

    public void setStarred(boolean starred) {
        this.starred = starred;
    }
}
