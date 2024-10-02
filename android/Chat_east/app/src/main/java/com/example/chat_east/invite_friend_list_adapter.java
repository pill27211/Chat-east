package com.example.chat_east;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class invite_friend_list_adapter extends RecyclerView.Adapter<invite_friend_list_adapter.friendviewholder> {

    private Cursor cursor;
    private Context context;
    private String my_user_id;
    private int chatroom_id;
    private OnItemClickListener on_item_click_listener;
    private chat_db_manager chat_db_manager;
    private Set<String> selectedFriends = new HashSet<>(); // 클릭된 friend_user_id를 저장할 리스트

    public interface OnItemClickListener {
        void onItemClick(String friend_user_id);
    }

    public invite_friend_list_adapter(Context context, Cursor cursor, String my_user_id, int chatroom_id, OnItemClickListener onitemclicklistener) {
        this.context = context;
        this.cursor = cursor;
        this.my_user_id = my_user_id;
        this.chatroom_id = chatroom_id;
        this.on_item_click_listener = onitemclicklistener;
        this.chat_db_manager = new chat_db_manager(context);
    }

    @NonNull
    @Override
    public friendviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewtype) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.invite_friend_list, parent, false);
        return new friendviewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull friendviewholder holder, int position) {
        if (cursor.moveToPosition(position)) {
            String profile_img_url = cursor.getString(cursor.getColumnIndexOrThrow("profile_img_url"));
            String friend_user_id = cursor.getString(cursor.getColumnIndexOrThrow("friend_user_id"));
            String nickname = cursor.getString(cursor.getColumnIndexOrThrow("nickname"));

            holder.friend_nickname.setText(nickname);
            holder.radio_button.setClickable(false);

            holder.itemView.setOnClickListener(v -> {
                if (on_item_click_listener != null) {
                    on_item_click_listener.onItemClick(friend_user_id);
                }
                if (selectedFriends.contains(friend_user_id)) {
                    selectedFriends.remove(friend_user_id);
                    holder.radio_button.setChecked(false);
                } else {
                    selectedFriends.add(friend_user_id);
                    holder.radio_button.setChecked(true);
                }
            });

            if (!Objects.equals(profile_img_url, "null")) {
                @SuppressLint("SdCardPath")
                String image_path = "/data/data/com.example.chat_east/cache/C_E_preview/preview_" + ExtractFileNameFromURL(profile_img_url) + ".jpg";
                File image_file = new File(image_path);

                if (!image_file.exists()) {
                    new save_image_task_profile(context, profile_img_url, new save_image_task_profile.SaveImageCallback() {
                        @Override
                        public void Onimagesaved(String profile_image_urls, String image_path) {
                            if (profile_img_url.equals(profile_image_urls)) {
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
        TextView friend_nickname;
        ImageView friend_profile_img;
        RadioButton radio_button;
        public friendviewholder(@NonNull View itemview) {
            super(itemview);
            friend_profile_img = itemview.findViewById(R.id.friend_image);
            friend_nickname = itemview.findViewById(R.id.friend_nickname);
            radio_button = itemview.findViewById(R.id.radio_button);
        }
    }

    public Set<String> getSelectedFriends() {
        return selectedFriends;
    }
}
