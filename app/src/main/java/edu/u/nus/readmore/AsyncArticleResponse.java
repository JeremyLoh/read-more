package edu.u.nus.readmore;

import java.util.Map;

public interface AsyncArticleResponse {
    void processFinish(Map<String, String> output);
}
