package edu.u.nus.readmore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class User implements Serializable {
    private String ID;
    // readList has a maximum size of 1000
    private final int READLIST_LIMIT = 1000;
    private List<Article> readList;
    private Map<String, Boolean> userFilter = new HashMap<>();

    public User() {
        // public no-arg constructor needed
        this.ID = "";
        readList = new ArrayList<>();
        userFilter.put("Arts", true);
        userFilter.put("History", true);
        userFilter.put("Math", true);
        userFilter.put("Science", true);

    }

    public User(String ID) {
        this.ID = ID;
        readList = new ArrayList<>();
        userFilter.put("Arts", true);
        userFilter.put("History", true);
        userFilter.put("Math", true);
        userFilter.put("Science", true);
    }

    public String getID() {
        return ID;
    }

    public List<Article> getReadList() {
        return readList;
    }

    public Map<String, Boolean> getUserFilter() { return userFilter; }

    public boolean hasReadArticle(Article article) {
        if (readList.size() == 0) {
            return false;
        }
        return readList.contains(article);
    }

    public void addReadArticle(Article article) {
        // check if limit has been reached
        if (READLIST_LIMIT == this.readList.size()) {
            // remove first article from readList
            readList.remove(0);
        }
        this.readList.add(article);
    }

    public Article getLatestArticle() {
        int readListSize = readList.size();
        if (readListSize == 0) {
            return null;
        } else {
            return readList.get(readListSize - 1);
        }
    }

    public void updateUserFilter(Map<String, Boolean> userFilter) {
        this.userFilter = userFilter;
    }
}