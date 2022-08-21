package edu.u.nus.readmore;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FetchArticleData extends AsyncTask<String, Void, Map<String, String>> {
    private final String MAX_IMAGE_SIZE_IN_PX = "1000";
    private Map<String, String> article = new HashMap<>();
    AsyncArticleResponse articleResponse;

    public FetchArticleData(AsyncArticleResponse asyncArticleResponse) {
        this.articleResponse = asyncArticleResponse;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        MainActivity.getActivityInstance().enableProgressBar(View.VISIBLE);
    }

    @Override
    protected void onPostExecute(Map<String, String> articleDetails) {
        articleResponse.processFinish(articleDetails);
        MainActivity.getActivityInstance().disableProgressBar(View.GONE);
        super.onPostExecute(articleDetails);
    }

    @Override
    protected Map<String, String> doInBackground(String... pageID) {
        String pageid = pageID[0];
        article.put("pageid", pageid);
        try {
            JSONObject page = getArticlePage(pageid);
            JSONObject imagePage = getImagePage(pageid);
            setArticleDetails(imagePage, page);
        } catch (MalformedURLException e) {
            // if url given is invalid
            e.printStackTrace();
        } catch (JSONException e) {
            // for invalid JSONArray
            e.printStackTrace();
        } catch (IOException e) {
            // could not query url
            e.printStackTrace();
        }
        return article;
    }

    private void setArticleDetails(JSONObject imagePage, JSONObject page) throws JSONException, IOException {
        article.put("imageURL", getImageUrlFromArticle(imagePage));
        article.put("title", page.getString("title"));
        article.put("URL", page.getString("fullurl"));
        article.put("description", getDescription(page));
    }

    @NonNull
    private String getArticleQuery(String pageid) {
        return "https://en.wikipedia.org/w/api.php?action=query&format=json" +
                "&pageids=" + pageid +
                "&prop=info|extracts" +
                "&inprop=url" +
                "&explaintext" +
                "&exintro=1";
    }

    @NonNull
    private JSONObject getArticlePage(String pageid) throws IOException, JSONException {
        String data = queryUrl(new URL(getArticleQuery(pageid)));
        JSONObject queryObj = (new JSONObject(data)).getJSONObject("query");
        JSONObject pages = queryObj.getJSONObject("pages");
        return pages.getJSONObject(pageid);
    }

    @NonNull
    private String getDescription(JSONObject page) throws JSONException {
        // Add additional newlines in extract, (after each paragraph)
        String extract = page.getString("extract").trim();
        return TextUtils.join("\n\n", extract.split("\n+"));
    }

    @NonNull
    private JSONObject getImagePage(String pageid) throws IOException, JSONException {
        String thumbnailImageQuery = getThumbnailImageQuery(pageid);
        String imageData = queryUrl(new URL(thumbnailImageQuery));
        return parsePageResult(pageid, imageData);
    }

    private String getImageUrlFromArticle(JSONObject page) throws JSONException, IOException {
        if (hasExistingThumbnail(page)) {
            return getThumbnailUrl(page);
        } else if (hasImages(page)) {
            return getImageUrl(page);
        }
        return "";
    }

    private String getImageUrl(JSONObject page) throws JSONException, IOException {
        // get title of first image file in images JSONArray, to conduct a query for it
        JSONArray images = page.getJSONArray("images");
        if (images.length() == 0) {
            return "";
        }
        String imageTitle = getFirstValidImageTitle(images);
        if (imageTitle.equals("")) {
            return "";
        }
        String imageTitleQuery = getImageTitleQuery(imageTitle);
        JSONObject imageJSON = new JSONObject(queryUrl(new URL(imageTitleQuery)));
        return getImageUrlFromImageTitleQuery(imageJSON);
    }

    @NonNull
    private String getImageUrlFromImageTitleQuery(JSONObject image) throws JSONException {
        JSONObject imageQuery = image.getJSONObject("query");
        JSONObject imagePages = imageQuery.getJSONObject("pages");
        JSONObject pageInfo = imagePages.getJSONObject("-1");
        return getThumbnailUrl(pageInfo);
    }

    private boolean hasImages(JSONObject pageResult) {
        return pageResult.has("images");
    }

    private String getFirstValidImageTitle(JSONArray images) throws JSONException {
        // Obtain first image that has a valid extension (not svg)
        String imageTitle = "";
        int size = images.length();
        for (int i = 0; i < size; i++) {
            JSONObject imageInfo = images.getJSONObject(i);
            String title = imageInfo.getString("title");
            String imageExtension = title.substring(title.length() - 3).toLowerCase();
            if (imageExtension.equals("jpg") || imageExtension.equals("png")) {
                imageTitle = title;
                break;
            }
        }
        return imageTitle;
    }

    @NonNull
    private String getThumbnailUrl(JSONObject page) throws JSONException {
        JSONObject imageInfo = page.getJSONObject("thumbnail");
        return imageInfo.getString("source");
    }

    private boolean hasExistingThumbnail(JSONObject pageIDObject) {
        return pageIDObject.has("thumbnail");
    }

    private JSONObject parsePageResult(String pageid, String imageData) throws JSONException {
        JSONObject queryObject = (new JSONObject(imageData)).getJSONObject("query");
        JSONObject pagesObject = queryObject.getJSONObject("pages");
        return pagesObject.getJSONObject(pageid);
    }

    @NonNull
    private String getImageTitleQuery(String imageTitle) {
        // Query in format: https://en.wikipedia.org/w/api.php?
        // action=query&format=json&prop=pageimages&pithumbsize=1000
        // &titles=Image:[IMAGE_FILE_TITLE]
        // Query image size max of 1000 (&pithumbsize=1000)
        return "https://en.wikipedia.org/w/api.php?action=query&format=json" +
                "&prop=pageimages" +
                "&pithumbsize=" + MAX_IMAGE_SIZE_IN_PX +
                "&titles=Image:" + imageTitle;
    }

    @NonNull
    private String getThumbnailImageQuery(String pageid) {
        // Query with valid thumbnail image (Example)
        // https://en.wikipedia.org/w/api.php?action=query&format=json&pageids=23420193&prop=pageimages|images&pithumbsize=1000
        // Query with no valid thumbnail image (Example)
        // https://en.wikipedia.org/w/api.php?action=query&format=json&pageids=9127632&prop=pageimages|images&pithumbsize=1000
        return "https://en.wikipedia.org/w/api.php?action=query&format=json" +
                "&pageids=" + pageid +
                "&prop=pageimages|images" +
                "&pithumbsize=" + MAX_IMAGE_SIZE_IN_PX;
    }

    // Returns a String representing a JSONObject
    private String queryUrl(URL URL) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) URL.openConnection();
        try {
            connection = (HttpURLConnection) URL.openConnection();
            return getStreamContents(connection.getInputStream());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }
        return "";
    }

    private String getStreamContents(InputStream inputStream) throws IOException {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream));
             Stream<String> lines = buffer.lines()) {
            return lines.collect(Collectors.joining());
        }
    }
}
