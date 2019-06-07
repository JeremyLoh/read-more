package edu.u.nus.readmore;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class FetchData extends AsyncTask<Void, Void, Void> {
    private List<String> Science = new ArrayList<>(Arrays.asList("Biology", "Physics"));
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            for (String topic : Science) {
                String data = "";
                StringBuilder urlStr = new StringBuilder("https://en.wikipedia.org/w/api.php?");
                StringBuilder modifier = new StringBuilder();
                modifier.append("action=query&format=json" +
                        "&list=categorymembers" +
                        "&cmlimit=max" +
                        "&cmtype=page" +
                        "&cmprop=ids" +
                        "&cmtitle=Category:" + topic);
                urlStr.append(modifier.toString());

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
                JSONObject jsonObject = (new JSONObject(data)).getJSONObject("query");
                JSONArray jsonArray = jsonObject.getJSONArray("categorymembers");
                int jsonArrayLength = jsonArray.length();
                List<String> pageidArr = new ArrayList<>();

                for (int i = 0; i < jsonArrayLength; i++) {
                    JSONObject obj = (JSONObject) jsonArray.get(i);
                    pageidArr.add(obj.getString("pageid"));
                }

                Map<String, Object> item = new HashMap<>();
                item.put("pageid", pageidArr);

                db.collection("Science")
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
}
