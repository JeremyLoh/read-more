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
    private int readIndex = -1;

    public User() {
        // public no-arg constructor needed
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

    public int getREADLIST_LIMIT() {
        return READLIST_LIMIT;
    }

    public int getReadIndex() {
        return readIndex;
    }

    public Map<String, Boolean> getUserFilter() {
        return userFilter;
    }

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
        if (readListSize == 0 || readIndex < 0) {
            return null;
        } else {
            return readList.get(readIndex);
        }
    }

    public Article getPreviousArticle() {
        if (readIndex > 0) {
            readIndex--;
            return readList.get(readIndex);
        } else {
            return null;
        }
    }

    public Article getNextArticle() {
        if (readIndex + 1 < readList.size()) {
            readIndex++;
            return readList.get(readIndex);
        } else {
            return null;
        }
    }

    public void incrementReadIndex() {
        if (readIndex + 1 < readList.size()) {
            readIndex++;
        }
    }

    public void updateUserFilter(Map<String, Boolean> userFilter) {
        this.userFilter = userFilter;
    }
}