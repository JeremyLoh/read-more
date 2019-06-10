package edu.u.nus.readmore;

class Article {
    private String title;
    private String description;
    private String pageid;
    private String URL;
    private String imageURL;

    public Article() {
        // public no-arg constructor needed
    }

    public Article(String title, String description, String pageid, String URL, String imageURL) {
        this.title = title;
        this.description = description;
        this.pageid = pageid;
        this.URL = URL;
        this.imageURL = imageURL;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPageid() {
        return pageid;
    }

    public String getURL() {
        return URL;
    }

    public String getImageURL() {
        return imageURL;
    }
}
