package edu.u.nus.readmore;

import android.content.res.AssetManager;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// Used to update db topics with text files placed in assets/ folder
class UpdateDbTopics extends AsyncTask<Void, Void, Void> {
    private List<String> categoryFiles = new ArrayList<>(
            Arrays.asList("Sports.txt"));
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private AssetManager assetManager;

    public UpdateDbTopics(Context context) {
        assetManager = context.getAssets();
    }

    // For changing UI
    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            processTopicCategoryFiles();
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
        return null;
    }

    private void processTopicCategoryFiles() throws IOException, JSONException {
        for (String fileName : categoryFiles) {
            List<String> topics = getTopics(fileName);
            if (topics == null) {
                continue;
            }
            saveTopics(topics, getCategoryName(fileName));
        }
    }

    private List<String> getTopics(String fileName) {
        try (BufferedReader buffer = new BufferedReader(
                new InputStreamReader(assetManager.open(fileName), StandardCharsets.UTF_8));
             Stream<String> lines = buffer.lines()) {
            return lines.collect(Collectors.toList());
        } catch (IOException e) {
            Log.e("Could not get topics from file: " + fileName, e.toString());
        }
        // when reading has failed, return null
        return null;
    }

    private void saveTopics(List<String> topicList, String categoryName) throws IOException, JSONException {
        for (final String topic : topicList) {
            URL url = getTopicUrl(topic);
            List<String> pageids = getPageIds(queryTopic(url));
            if (pageids.size() == 0) {
                continue;
            }
            saveTopicToDb(categoryName, topic, pageids);
        }
    }

    @NonNull
    private URL getTopicUrl(String topic) throws MalformedURLException {
        String url = "https://en.wikipedia.org/w/api.php?" +
                "action=query&format=json" +
                "&list=categorymembers" +
                "&cmlimit=max" +
                "&cmtype=page" +
                "&cmprop=ids" +
                "&cmtitle=Category:" + topic;
        return new URL(url);
    }

    @NonNull
    private JSONObject queryTopic(URL url) throws IOException, JSONException {
        StringBuilder data = new StringBuilder();
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        try (InputStream inputStream = httpURLConnection.getInputStream();
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line = "";
            while (line != null) {
                line = bufferedReader.readLine();
                data.append(line);
            }
        }
        return new JSONObject(data.toString());
    }

    private void saveTopicToDb(String categoryName, String topic, List<String> pageids) {
        DocumentReference document = db.collection(categoryName).document(topic);
        Map<String, Object> item = new HashMap<>();
        item.put("pageid", pageids);
        item.put("ID", document.getId());
        document.set(item, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.v("Complete", "Added " + topic))
                .addOnFailureListener(e -> Log.v("Failed", e.toString()));
    }

    @NonNull
    private List<String> getPageIds(JSONObject response) throws JSONException {
        JSONObject query = response.getJSONObject("query");
        JSONArray categoryMembers = query.getJSONArray("categorymembers");
        List<String> pageids = new ArrayList<>();
        for (int i = 0; i < categoryMembers.length(); i++) {
            JSONObject obj = (JSONObject) categoryMembers.get(i);
            pageids.add(obj.getString("pageid"));
        }
        return pageids;
    }

    @NonNull
    private String getCategoryName(String fileName) {
        // Remove file extension ".txt"
        return fileName.substring(0, fileName.length() - 4);
    }
}
