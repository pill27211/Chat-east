package com.example.chat_east;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.Objects;

public class profile_adapter extends RecyclerView.Adapter<profile_adapter.Friendviewholder> {

    private Cursor cursor;
    private Context context;

    public profile_adapter(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
    }

    @NonNull
    @Override
    public Friendviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewtype) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_profile, parent, false);
        return new Friendviewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Friendviewholder holder, int position) {
        if (cursor.moveToPosition(position)) {
            String profile_img_url = cursor.getString(cursor.getColumnIndexOrThrow("profile_img_url"));
            String nickname = cursor.getString(cursor.getColumnIndexOrThrow("nickname"));
            String comment = cursor.getString(cursor.getColumnIndexOrThrow("comment"));
            if (Objects.equals(comment, "null"))comment ="";
            holder.my_nickname.setText(nickname);
            holder.my_comment.setText(comment);

            if(!Objects.equals(profile_img_url, "null")){
                @SuppressLint("SdCardPath")
                String image_path = "/data/data/com.example.chat_east/cache/C_E_preview/preview_" + ExtractFileNameFromURL(profile_img_url) + ".jpg";
                File image_file = new File(image_path);

                if (!image_file.exists()) {
                    new save_image_task_profile(context, profile_img_url, new save_image_task_profile.SaveImageCallback() {
                        @Override
                        public void Onimagesaved(String profile_image_urls, String image_path) {
                            if (profile_img_url == profile_image_urls) {
                                Glide.with(context)
                                        .load(image_path)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL) // 캐싱 전략 설정
                                        .into(holder.my_profile_img);
                            }
                        }
                    }).execute(profile_img_url, "preview_" + ExtractFileNameFromURL(profile_img_url) + ".jpg");
                } else {
                    Glide.with(context)
                            .load(image_path)
                            .diskCacheStrategy(DiskCacheStrategy.ALL) // 캐싱 전략 설정
                            .into(holder.my_profile_img);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    private String ExtractFileNameFromURL(String url) {
        String[] parts = url.split("/images/");
        if (parts.length > 1) {
            String filepart = parts[1];
            int dotindex = filepart.indexOf('.');
            if (dotindex != -1) {
                return filepart.substring(0, dotindex);
            }
        }
        return "";
    }

    static class Friendviewholder extends RecyclerView.ViewHolder {
        ImageView my_profile_img;
        TextView my_nickname, my_comment;

        public Friendviewholder(@NonNull View itemview) {
            super(itemview);
            my_profile_img = itemview.findViewById(R.id.profile_image);
            my_nickname = itemview.findViewById(R.id.friend_name);
            my_comment = itemview.findViewById(R.id.friend_comment);
        }
    }
}
