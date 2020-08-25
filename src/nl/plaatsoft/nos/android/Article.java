package nl.plaatsoft.nos.android;

public class Article {
    private final String title;
    private final String imageUrl;
    private final String date;
    private final String content;

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
