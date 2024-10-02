package com.example.chat_east;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

public class save_image_task_download extends AsyncTask<String, Void, File> {
    private Context context;
    private String img_url;
    private SaveImageCallback callback;

    public interface SaveImageCallback {
        void Onimagesaved(String img_url, String image_path);
    }

    public save_image_task_download(Context context, String img_url, SaveImageCallback callback) {
        this.context = context;
        this.img_url = img_url;
        this.callback = callback;
    }

    @Override
    protected File doInBackground(String... params) {
        String image_url = params[0];
        String file_name = params[1];
        Log.d("image_url",image_url);
        Log.d("file_name",file_name);
        return image_util_download.SaveImage(context, image_url, file_name);
    }

    @Override
    protected void onPostExecute(File result) {
        super.onPostExecute(result);
        if (result != null) {
            if (callback != null) {
                callback.Onimagesaved(img_url, result.getAbsolutePath());
            }
            Toast.makeText(context, "이미지 저장이 완료되었습니다.", Toast.LENGTH_LONG).show();
        }
    }
}

