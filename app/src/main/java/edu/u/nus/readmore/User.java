package edu.u.nus.readmore;

import java.util.ArrayList;
import java.util.List;

class User {
    private String ID;
    // readList has a maximum size of 100
    private final int READLIST_LIMIT = 100;
    private List<Article> readList;

    public User() {
        // public no-arg constructor needed
        this.ID = "";
        readList = new ArrayList<>();
    }

    public User(String ID) {
        this.ID = ID;
        readList = new ArrayList<>();
    }

    public String getID() {
        return ID;
    }

    public List<Article> getReadList() {
        return readList;
    }

    public void addReadArticle(Article article) {
        // check if limit has been reached
        if (READLIST_LIMIT == this.readList.size()) {
            // remove first article from readList
            readList.remove(0);
        }
        this.readList.add(article);
    }
}
