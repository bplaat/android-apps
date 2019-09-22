package nl.nos.android;

import java.io.Serializable;

public class Article implements Serializable {
    private String title;
    private String imageUrl;
    private String date;
    private String content;

    public Article(String title, String imageUrl, String date, String content) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.date = date;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDate() {
        return date;
    }

    public String getContent() {
        return content;
    }
}
