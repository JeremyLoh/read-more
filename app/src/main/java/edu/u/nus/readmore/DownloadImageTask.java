package edu.u.nus.readmore;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    ImageView imageView;

    public DownloadImageTask(ImageView imageView) {
        this.imageView = imageView;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        MainActivity.getActivityInstance().disableImage();
    }

    @Override
    protected Bitmap doInBackground(String... URL) {
        String imageURL = URL[0];
        Bitmap image = null;
        try {
            InputStream inputStream = new java.net.URL(imageURL).openStream();
            image = BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            // For InputStream exception
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return image;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        MainActivity.getActivityInstance().enableImage();
        super.onPostExecute(bitmap);
    }
}
