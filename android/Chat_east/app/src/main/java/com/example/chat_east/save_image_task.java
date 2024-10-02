package com.example.chat_east;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;

public class save_image_task extends AsyncTask<String, Void, File> {
    private Context context;
    private int message_id;
    private SaveImageCallback callback;

    public interface SaveImageCallback {
        void Onimagesaved(int message_id, String image_path);
    }

    public save_image_task(Context context, int message_id, SaveImageCallback callback) {
        this.context = context;
        this.message_id = message_id;
        this.callback = callback;
    }

    @Override
    protected File doInBackground(String... params) {
        String image_url = params[0];
        String file_name = params[1];
        return image_util.SaveImage(context, image_url, file_name);
    }

    @Override
    protected void onPostExecute(File result) {
        super.onPostExecute(result);
        if (result != null) {
            if (callback != null) {
                callback.Onimagesaved(message_id, result.getAbsolutePath());
            }
        }
    }
}

