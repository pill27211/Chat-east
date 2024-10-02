package com.example.chat_east;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class chatroom_img_adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_DATE = 0;
    private static final int TYPE_PHOTO = 1;
    PhotoClick photo_click;
    Context context;
    private List<Object> item_list;

    public chatroom_img_adapter(Context context, List<Object> item_list) {
        this.context = context;
        this.item_list = item_list;
    }

    public interface PhotoClick {
        void onItemClick(String img_url, int message_id);
    }

    @Override
    public int getItemViewType(int position) {
        if (item_list.get(position) instanceof String) {
            return TYPE_DATE;
        } else {
            return TYPE_PHOTO;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_DATE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date, parent, false);
            return new date_viewhold(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
            return new photo_viewhold(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof date_viewhold) {
            ((date_viewhold) holder).bind((String) item_list.get(position));
        } else {
            chatroom_img.Photo photo = (chatroom_img.Photo) item_list.get(position);
            ((photo_viewhold) holder).bind(photo, photo_click);
        }
    }

    @Override
    public int getItemCount() {
        return item_list.size();
    }

    public class date_viewhold extends RecyclerView.ViewHolder {
        TextView date_textview;

        public date_viewhold(View itemView) {
            super(itemView);
            date_textview = itemView.findViewById(R.id.date_textview);
        }

        public void bind(String date) {
            date_textview.setText(date);
        }
    }

    public class photo_viewhold extends RecyclerView.ViewHolder {
        ImageView photo_imageview;

        public photo_viewhold(View itemView) {
            super(itemView);
            photo_imageview = itemView.findViewById(R.id.phto_imageview);
        }

        public void bind(chatroom_img.Photo photo, PhotoClick listener) {
            String content = photo.content;
            int message_id = photo.message_id;
            if(!Objects.equals(content, "null")) {
                @SuppressLint("SdCardPath")
                String image_path = "/data/data/com.example.chat_east/cache/C_E_preview/preview_" + String.valueOf(message_id) + ".jpg";
                File image_file = new File(image_path);

                if (!image_file.exists()) {
                    new save_image_task(context, message_id, new save_image_task.SaveImageCallback() {
                        @Override
                        public void Onimagesaved(int messages_id, String image_path) {
                            if (messages_id == message_id) {
                                Glide.with(context)
                                        .load(image_path)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL) // 캐싱 전략 설정
                                        .into(photo_imageview);
                            }
                        }
                    }).execute(content, "preview_" + message_id + ".jpg");
                } else {
                    Glide.with(context)
                            .load(image_path)
                            .diskCacheStrategy(DiskCacheStrategy.ALL) // 캐싱 전략 설정
                            .into(photo_imageview);
                }

                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(content, message_id);
                    }
                });
            }
        }
    }

}

