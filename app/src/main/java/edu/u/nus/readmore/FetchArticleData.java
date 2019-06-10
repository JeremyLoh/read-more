package edu.u.nus.readmore;

import android.os.AsyncTask;

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

public class FetchArticleData extends AsyncTask<String, Void, Map<String, String>> {
    public AsyncArticleResponse articleResponse = null;
    Map<String, String> output = new HashMap<>();

    @Override
    protected Map<String, String> doInBackground(String... pageID) {
        try {
            String data = "";
            StringBuilder urlStr = new StringBuilder("https://en.wikipedia.org/w/api.php?");
            StringBuilder modifier = new StringBuilder();
            modifier.append("action=query&format=json" +
                    "&pageids=" +
                    pageID[0] +
                    "&prop=info|extracts" +
                    "&inprop=url" +
                    "&explaintext" +
                    "&exintro=1");
            urlStr.append(modifier);
            URL url = new URL(urlStr.toString());
            // create a connection for url
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = httpURLConnection.getInputStream();
            // Use BufferedReader to read data from InputStream
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            while (line != null) {
                line = bufferedReader.readLine();
                data += line;
            }

            // Parse data obtained
            JSONObject queryObj = (new JSONObject(data)).getJSONObject("query");
            JSONObject pagesObj = queryObj.getJSONObject("pages");
            JSONObject pageIDObj = pagesObj.getJSONObject(pageID[0]);
            String title = pageIDObj.getString("title");
            String fullURL = pageIDObj.getString("fullurl");
            String extract = pageIDObj.getString("extract");
            output.put("title", title);
            output.put("URL", fullURL);
            output.put("description", extract);
        } catch (MalformedURLException e) {
            // if url given is invalid
            e.printStackTrace();
        } catch (IOException e) {
            // for invalid url.openConnection() function
            e.printStackTrace();
        } catch (JSONException e) {
            // for invalid JSONArray
            e.printStackTrace();
        }

        return output;
    }

    @Override
    protected void onPostExecute(Map<String, String> stringStringMap) {
        articleResponse.processFinish(stringStringMap);
        super.onPostExecute(stringStringMap);
    }
}
