package nl.nos.android;

import java.io.Serializable;

public class Item implements Serializable {
    private String title;
    private String image;
    private String date;
    private String content;

    public Item(String title, String image, String date, String content) {
        this.title = title;
        this.image = image;
        this.date = date;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public String getImage() {
        return image;
    }

    public String getDate() {
        return date;
    }

    public String getContent() {
        return content;
    }
}
