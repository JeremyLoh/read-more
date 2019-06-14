package edu.u.nus.readmore;

import android.os.AsyncTask;
import android.text.TextUtils;

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
import java.util.Random;

public class FetchArticleData extends AsyncTask<String, Void, Map<String, String>> {
    AsyncArticleResponse articleResponse = null;
    private Map<String, String> output = new HashMap<>();

    public FetchArticleData(AsyncArticleResponse asyncArticleResponse) {
        this.articleResponse = asyncArticleResponse;
    }

    @Override
    protected Map<String, String> doInBackground(String... pageID) {
        try {
            String pageid = pageID[0];
            output.put("pageid", pageid);
            // Obtain largest possible image from query
            // Query with valid thumbnail image (Example)
            // https://en.wikipedia.org/w/api.php?action=query&format=json&pageids=23420193&prop=pageimages|images&pithumbsize=1000
            // Query with no valid thumbnail image (Example)
            // https://en.wikipedia.org/w/api.php?action=query&format=json&pageids=9127632&prop=pageimages|images&pithumbsize=1000
            String imageQuery = "https://en.wikipedia.org/w/api.php?" +
                    "action=query&format=json" +
                    "&pageids=" +
                    pageid +
                    "&prop=pageimages|images" +
                    "&pithumbsize=1000"; // set max image size obtained to be within 1000px
            String imageData = queryURL(new URL(imageQuery));
            // parse data obtained
            JSONObject queryObject = (new JSONObject(imageData)).getJSONObject("query");
            JSONObject pagesObject = queryObject.getJSONObject("pages");
            JSONObject pageIDObject = pagesObject.getJSONObject(pageid);
            // check for existing thumbnail JSON object (from pageimages query)
            if (pageIDObject.has("thumbnail")) {
                JSONObject imageInfo = pageIDObject.getJSONObject("thumbnail");
                String imageSrc = imageInfo.getString("source");
                output.put("imageURL", imageSrc);
            } else {
                // check for existing images JSONArray
                if (pageIDObject.has("images")) {
                    // get title of first image file in images JSONArray,
                    // to conduct a query for it
                    JSONArray imageArray = pageIDObject.getJSONArray("images");
                    int imageArraySize = imageArray.length();
                    if (imageArraySize >= 1) {
                        // Obtain first image that has a valid extension (not svg)
                        String imageTitle = "";
                        boolean foundValidImage = false;
                        for (int index = 0; index < imageArraySize; index++) {
                            JSONObject imageInfo = imageArray.getJSONObject(index);
                            imageTitle = imageInfo.getString("title");
                            // check image extension
                            String imageExtension = imageTitle.substring(imageTitle.length() - 3);
                            if (imageExtension.equals("jpg") || imageExtension.equals("png")) {
                                foundValidImage = true;
                                break;
                            }
                        }
                        if (foundValidImage) {
                            // Query in format: https://en.wikipedia.org/w/api.php?
                            // action=query&format=json&prop=pageimages&pithumbsize=1000
                            // &titles=Image:[IMAGE_FILE_TITLE]
                            // Query image size max of 1000 (&pithumbsize=1000)
                            String imageTitleQuery = "https://en.wikipedia.org/w/api.php?" +
                                    "action=query&format=json" +
                                    "&prop=pageimages" +
                                    "&pithumbsize=1000" +
                                    "&titles=Image:" + imageTitle;
                            JSONObject imageJSON = new JSONObject(queryURL(new URL(imageTitleQuery)));
                            // Retrieving image link through obtained JSON (imageJSON)
                            JSONObject imageQueryObj = imageJSON.getJSONObject("query");
                            JSONObject imagePagesObj = imageQueryObj.getJSONObject("pages");
                            JSONObject pagesInfoObj = imagePagesObj.getJSONObject("-1");
                            JSONObject thumbnailObj = pagesInfoObj.getJSONObject("thumbnail");
                            String imageSrc = thumbnailObj.getString("source");
                            output.put("imageURL", imageSrc);
                        } else {
                            // No image available
                            output.put("imageURL", "");
                        }
                    } else {
                        // No image available
                        output.put("imageURL", "");
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
        }
        return output;
    }

    // Returns a String representing a JSONObject
    private String queryURL(URL URL) {
        try {
            StringBuilder output = new StringBuilder();
            // create a connection for URL
            HttpURLConnection httpImageURLConnection = (HttpURLConnection) URL.openConnection();
            InputStream inputImageStream = httpImageURLConnection.getInputStream();
            // Use BufferedReader to read data from InputStream
            BufferedReader imageBufferedReader = new BufferedReader(new InputStreamReader(inputImageStream));
            String readContent = imageBufferedReader.readLine();
            while (readContent != null) {
                output.append(readContent);
                readContent = imageBufferedReader.readLine();
            }
            return output.toString();
        } catch (MalformedURLException e) {
            // if url given is invalid
            e.printStackTrace();
        } catch (IOException e) {
            // for invalid url.openConnection() function
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void onPostExecute(Map<String, String> stringStringMap) {
        articleResponse.processFinish(stringStringMap);
        super.onPostExecute(stringStringMap);
    }
}
