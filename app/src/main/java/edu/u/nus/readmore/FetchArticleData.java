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
    AsyncArticleResponse articleResponse = null;
    private Map<String, String> output = new HashMap<>();

    public FetchArticleData(AsyncArticleResponse asyncArticleResponse) {
        this.articleResponse = asyncArticleResponse;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        MainActivity.getActivityInstance().enableProgressBar(View.VISIBLE);
    }

    @Override
    protected Map<String, String> doInBackground(String... pageID) {
        String pageid = pageID[0];
        output.put("pageid", pageid);
        try {
            String thumbnailImageQuery = getThumbnailImageQuery(pageid);
            String imageData = queryURL(new URL(thumbnailImageQuery));
            JSONObject pageResult = parsePageResult(pageid, imageData);
            if (hasExistingThumbnail(pageResult)) {
                output.put("imageURL", getThumbnailUrl(pageResult));
            } else {
                // check for existing images JSONArray
                if (hasImages(pageResult)) {
                    // get title of first image file in images JSONArray,
                    // to conduct a query for it
                    JSONArray imageArray = pageResult.getJSONArray("images");
                    int imageArraySize = imageArray.length();
                    if (imageArraySize == 0) {
                        // No image available
                        output.put("imageURL", "");
                    }
                    if (imageArraySize >= 1) {
                        // Obtain first image that has a valid extension (not svg)
                        String imageTitle = getFirstValidImageTitle(imageArray);
                        boolean foundValidImage = !imageTitle.equals("");
                        if (foundValidImage) {
                            String imageTitleQuery = getImageTitleQuery(imageTitle);
                            JSONObject imageJSON = new JSONObject(queryURL(new URL(imageTitleQuery)));
                            output.put("imageURL", getImageUrl(imageJSON));
                        } else {
                            // No image available
                            output.put("imageURL", "");
                        }
                    }
                } else {
                    // No image available
                    output.put("imageURL", "");
                }
            }
            // fetch article query
            String articleQuery = "https://en.wikipedia.org/w/api.php?" +
                    "action=query&format=json" +
                    "&pageids=" + pageid +
                    "&prop=info|extracts" +
                    "&inprop=url" +
                    "&explaintext" +
                    "&exintro=1";
            String data = queryURL(new URL(articleQuery));
            // Parse data obtained
            JSONObject queryObj = (new JSONObject(data)).getJSONObject("query");
            JSONObject pagesObj = queryObj.getJSONObject("pages");
            JSONObject pageIDObj = pagesObj.getJSONObject(pageid);
            String title = pageIDObj.getString("title");
            String fullURL = pageIDObj.getString("fullurl");
            String extract = pageIDObj.getString("extract");
            // Add additional newlines in extract, (after each paragraph)
            // remove whitespace at start and end
            extract = TextUtils.join("\n\n", (extract.trim()).split("\n+"));
            output.put("title", title);
            output.put("URL", fullURL);
            output.put("description", extract);
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
        return output;
    }

    @NonNull
    private String getImageUrl(JSONObject image) throws JSONException {
        JSONObject imageQuery = image.getJSONObject("query");
        JSONObject imagePages = imageQuery.getJSONObject("pages");
        JSONObject pageInfo = imagePages.getJSONObject("-1");
        return getThumbnailUrl(pageInfo);
    }

    private boolean hasImages(JSONObject pageResult) {
        return pageResult.has("images");
    }

    private String getFirstValidImageTitle(JSONArray images) throws JSONException {
        String imageTitle = "";
        int size = images.length();
        for (int index = 0; index < size; index++) {
            JSONObject imageInfo = images.getJSONObject(index);
            String title = imageInfo.getString("title");
            String imageExtension = title.substring(title.length() - 3);
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
        // Obtain largest possible image from query
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
    private String queryURL(URL URL) throws IOException {
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

    @Override
    protected void onPostExecute(Map<String, String> stringStringMap) {
        articleResponse.processFinish(stringStringMap);
        MainActivity.getActivityInstance().disableProgressBar(View.GONE);
        super.onPostExecute(stringStringMap);
    }
}
