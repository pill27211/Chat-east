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

public class friend_list_adapter extends RecyclerView.Adapter<friend_list_adapter.friendviewholder> {

    private Cursor cursor;
    private Context context;
    private OnItemClickListener on_item_click_listener;

    public interface OnItemClickListener {
        void onItemClick(String friend_user_id);
    }

    public friend_list_adapter(Context context, Cursor cursor, OnItemClickListener onitemclicklistener) {
        this.context = context;
        this.cursor = cursor;
        this.on_item_click_listener = onitemclicklistener;
    }

    @NonNull
    @Override
    public friendviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewtype) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_list, parent, false);
        return new friendviewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull friendviewholder holder, int position) {
        if (cursor.moveToPosition(position)) {
            String profile_img_url = cursor.getString(cursor.getColumnIndexOrThrow("profile_img_url"));
            String friend_user_id = cursor.getString(cursor.getColumnIndexOrThrow("friend_user_id"));
            String nickname = cursor.getString(cursor.getColumnIndexOrThrow("nickname"));
            String comment = cursor.getString(cursor.getColumnIndexOrThrow("comment"));
            if (Objects.equals(comment, "null")) comment = "";
            holder.friend_nickname.setText(nickname);
            holder.friend_comment.setText(comment);

            holder.itemView.setOnClickListener(v -> {
                if (on_item_click_listener != null) {
                    on_item_click_listener.onItemClick(friend_user_id);
                }
            });

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
                                        .into(holder.friend_profile_img);
                            }
                        }
                    }).execute(profile_img_url, "preview_" + ExtractFileNameFromURL(profile_img_url) + ".jpg");
                } else {
                    Glide.with(context)
                            .load(image_path)
                            .diskCacheStrategy(DiskCacheStrategy.ALL) // 캐싱 전략 설정
                            .into(holder.friend_profile_img);
                }
            }

        }
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

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    static class friendviewholder extends RecyclerView.ViewHolder {
        TextView friend_nickname, friend_comment;
        ImageView friend_profile_img;

        public friendviewholder(@NonNull View itemview) {
            super(itemview);
            friend_profile_img = itemview.findViewById(R.id.profile_image);
            friend_nickname = itemview.findViewById(R.id.friend_name);
            friend_comment = itemview.findViewById(R.id.friend_comment);
        }
    }
}
