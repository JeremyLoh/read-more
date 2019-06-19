package edu.u.nus.readmore;

import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.util.Util;

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

class FetchData extends AsyncTask<Void, Void, Void> {
    private List<String> categoryFiles = new ArrayList<>(
            Arrays.asList("science.txt"));
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private AssetManager assetManager;

    public FetchData(Context context) {
        assetManager = context.getAssets();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            // Read txt file for topics
            for (String fileName : categoryFiles) {
                List<String> topicList = getTopicList(fileName);
                // categoryName does not contain ".txt"
                String categoryName = fileName.substring(0, fileName.length() - 4);
                if (topicList != null) {
                    // Read and store topics
                    for (String topic : topicList) {
                        String data = "";
                        String urlStr = "https://en.wikipedia.org/w/api.php?" +
                                "action=query&format=json" +
                                "&list=categorymembers" +
                                "&cmlimit=max" +
                                "&cmtype=page" +
                                "&cmprop=ids" +
                                "&cmtitle=Category:" + topic;
                        URL url = new URL(urlStr);
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
                        JSONObject jsonObject = (new JSONObject(data)).getJSONObject("query");
                        JSONArray jsonArray = jsonObject.getJSONArray("categorymembers");
                        int jsonArrayLength = jsonArray.length();
                        List<String> pageidArr = new ArrayList<>();

                        for (int i = 0; i < jsonArrayLength; i++) {
                            JSONObject obj = (JSONObject) jsonArray.get(i);
                            pageidArr.add(obj.getString("pageid"));
                        }

                        if (pageidArr.size() == 0) {
                            continue;
                        }

                        Map<String, Object> item = new HashMap<>();
                        item.put("pageid", pageidArr);
                        // generate random id
                        item.put("ID", Util.autoId());

                        db.collection(categoryName)
                                .document(topic)
                                .set(item, SetOptions.merge())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.v("Complete", "doInBackground");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.v("Failed", e.toString());
                                    }
                                });
                    }
                }
            }
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

    // For changing UI
    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }

    private List<String> getTopicList(String fileName) {
        BufferedReader bufferedReader = null;
        try {
            List<String> outputList = new ArrayList<>();
            bufferedReader = new BufferedReader(
                    new InputStreamReader(assetManager.open(fileName), StandardCharsets.UTF_8));
            String line = bufferedReader.readLine();
            while (line != null) {
                outputList.add(line);
                line = bufferedReader.readLine();
            }
            return outputList;
        } catch (IOException e) {
            Log.e("BufferedReader Error", e.toString());
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    Log.e("BufferedReader Error", e.toString());
                }
            }
        }
        // when reading has failed, return null
        return null;
    }
}
