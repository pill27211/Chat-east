package com.example.chat_east;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.Objects;

public class side_bar_friend_list_adapter extends RecyclerView.Adapter<side_bar_friend_list_adapter.Chatroomlistvh> {
    private chat_db_manager db_manager;
    private Cursor cursor;
    private Context context;
    private side_bar_friend_list_adapter.OnItemClickListener on_item_click_listener;
    private side_bar_friend_list_adapter.Addfriend addfriend_listener;

    public interface OnItemClickListener {
        void onItemClick(String friend_id);
    }

    public interface Addfriend {
        void Addfriend(String friend_id);
    }

    public side_bar_friend_list_adapter(Context context, Cursor cursor, side_bar_friend_list_adapter.OnItemClickListener onitemclicklistener, side_bar_friend_list_adapter.Addfriend addfriendlistener) {
        this.context = context;
        this.cursor = cursor;
        this.on_item_click_listener = onitemclicklistener;
        this.addfriend_listener = addfriendlistener;
        db_manager = new chat_db_manager(context);
    }

    @NonNull
    @Override
    public Chatroomlistvh onCreateViewHolder(@NonNull ViewGroup parent, int viewtype) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sidebar_friend_list, parent, false);
        return new Chatroomlistvh(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Chatroomlistvh holder, int position) {
        if (cursor.moveToPosition(position)) {
            String user_id = cursor.getString(cursor.getColumnIndexOrThrow("friend_user_id"));
            String nickname = cursor.getString(cursor.getColumnIndexOrThrow("nickname"));
            String profile_img_url = cursor.getString(cursor.getColumnIndexOrThrow("profile_img_url"));
            int friends_type = cursor.getInt(cursor.getColumnIndexOrThrow("friends_type"));

            holder.side_bar_add_friend_icon.setVisibility(View.GONE);
            if(friends_type==0){
                holder.side_bar_add_friend_icon.setVisibility(View.VISIBLE);
            }
            holder.sidebar_nickname.setText(nickname);

            holder.side_bar_friend.setOnClickListener(v -> {
                if (on_item_click_listener != null) {
                    on_item_click_listener.onItemClick(user_id);
                }
            });

            holder.side_bar_add_friend_icon.setOnClickListener(v -> {
                if (addfriend_listener != null) {
                    addfriend_listener.Addfriend(user_id);
                    holder.side_bar_add_friend_icon.setVisibility(View.GONE);
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
                                        .into(holder.friend_profile_image);
                            }
                        }
                    }).execute(profile_img_url, "preview_" + ExtractFileNameFromURL(profile_img_url) + ".jpg");
                } else {
                    Glide.with(context)
                            .load(image_path)
                            .diskCacheStrategy(DiskCacheStrategy.ALL) // 캐싱 전략 설정
                            .into(holder.friend_profile_image);
                }
            }

        }
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    private String ExtractFileNameFromURL(String url) {
        // URL에서 "images/" 뒤의 부분만 추출
        String[] parts = url.split("/images/");
        if (parts.length > 1) {
            // 부분을 추출하여 ".jpg"를 제외한 숫자만 얻기
            String filepart = parts[1];
            int dotindex = filepart.indexOf('.');
            if (dotindex != -1) {
                return filepart.substring(0, dotindex); // ".jpg"를 제외한 숫자만 추출
            }
        }
        return ""; // 추출 실패 시 빈 문자열 반환
    }

    static class Chatroomlistvh extends RecyclerView.ViewHolder {
        LinearLayout side_bar_friend;
        ImageView friend_profile_image;
        TextView sidebar_nickname;
        CardView side_bar_add_friend_icon;
        public Chatroomlistvh(@NonNull View itemview) {
            super(itemview);
            side_bar_friend = itemview.findViewById(R.id.side_bar_friend);
            friend_profile_image = itemview.findViewById(R.id.friend_profile_image);
            sidebar_nickname = itemview.findViewById(R.id.sidebar_nickname);
            side_bar_add_friend_icon = itemview.findViewById(R.id.side_bar_add_friend_icon);
        }
    }

    public void close() {
        if (db_manager != null) {
            db_manager.close();
        }
    }

}
