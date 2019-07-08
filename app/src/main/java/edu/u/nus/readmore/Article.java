package edu.u.nus.readmore;

import java.io.Serializable;

public class Article implements Serializable {
    private String title;
    private String description;
    private String pageid;
    private String url;
    private String imageURL;

    public Article() {
        // public no-arg constructor needed
    }

    public Article(String title, String description, String pageid, String url, String imageURL) {
        this.title = title;
        this.description = description;
        this.pageid = pageid;
        this.url = url;
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

    public String getUrl() { return url; }

    public String getImageURL() {
        return imageURL;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((pageid == null) ? 0 : pageid.hashCode());
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        result = prime * result + ((imageURL == null) ? 0 : imageURL.hashCode());
        return result;
    }

    @Override
    public boolean equals(@androidx.annotation.Nullable Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (obj instanceof Article) {
            Article otherArticle = (Article) obj;
            return pageid.equals(otherArticle.getPageid());
        } else {
            return false;
        }
    }
}
