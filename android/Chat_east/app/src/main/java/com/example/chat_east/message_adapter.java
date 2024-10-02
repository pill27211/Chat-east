package com.example.chat_east;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Environment;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class message_adapter extends RecyclerView.Adapter<message_adapter.Messagelistvh> {
    private chat_db_manager chat_db_manager;
    private Cursor cursor;
    private Context context;
    private int chatroom_id;
    private String my_user_id;
    private message_adapter.OnItemClickListener on_item_click_listener;
    private message_adapter.PhotoClick photoclick;
    private message_adapter.FileDownload filedownload;
    private message_adapter.FileClick fileclick;
    private message_adapter.CallClick callclick;


    public interface OnItemClickListener {
        void onItemClick(String user_id);
    }

    public interface PhotoClick{
        void onItemClick(String img_url, int message_id);
    }

    public interface FileDownload{
        void onItemClick(String file_url, String file_name, int position);
    }

    public interface FileClick{
        void onItemClick(String file_name);
    }

    public interface CallClick{
        void onItemClick(String call_from_id,String call_to_id);
    }

    public message_adapter(Context context, Cursor cursor, int chatroom_id, String my_user_id, message_adapter.OnItemClickListener onitemclicklistener, message_adapter.PhotoClick photoclick, message_adapter.FileDownload filedownload, message_adapter.FileClick fileclick, message_adapter.CallClick callclick) {
        this.context = context;
        this.cursor = cursor;
        this.on_item_click_listener = onitemclicklistener;
        this.photoclick = photoclick;
        this.filedownload = filedownload;
        this.fileclick = fileclick;
        this.chatroom_id = chatroom_id;
        this.my_user_id = my_user_id;
        this.callclick = callclick;
        chat_db_manager = new chat_db_manager(context);
    }

    @NonNull
    @Override
    public Messagelistvh onCreateViewHolder(@NonNull ViewGroup parent, int viewtype) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_layout, parent, false);
        return new Messagelistvh(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Messagelistvh holder, int position) {
        if (cursor.moveToPosition(position)) {
            String user_id = cursor.getString(cursor.getColumnIndexOrThrow("user_id"));
            int message_id = cursor.getInt(cursor.getColumnIndexOrThrow("message_id"));
            String content = cursor.getString(cursor.getColumnIndexOrThrow("content"));
            String create_time = cursor.getString(cursor.getColumnIndexOrThrow("create_time"));
            int message_type = cursor.getInt(cursor.getColumnIndexOrThrow("message_type"));

            holder.chatting_date.setVisibility(View.GONE);
            holder.other_chatting.setVisibility(View.GONE);
            holder.my_chatting.setVisibility(View.GONE);
            holder.up_margin.setVisibility(View.GONE);
            holder.my_photo.setVisibility(View.GONE);
            holder.friend_photo.setVisibility(View.GONE);
            holder.friend_file.setVisibility(View.GONE);
            holder.my_file.setVisibility(View.GONE);
            holder.my_comment.setVisibility(View.GONE);
            holder.friend_photo.layout(0,0,0,0);
            holder.friend_call.setVisibility(View.GONE);
            holder.my_call.setVisibility(View.GONE);
            holder.message_start_me.setVisibility(View.GONE);

            if (chat_db_manager.IsDateChange(chatroom_id, message_id, create_time)) {
                holder.chatting_date.setVisibility(View.VISIBLE);
                SimpleDateFormat input_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat output_format = new SimpleDateFormat("yyyy년 M월 d일 EEEE");
                Date date = null;
                try {
                    date = input_format.parse(create_time);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                String format_date = output_format.format(date);
                holder.chatting_date.setText(format_date);
            }

            Glide.with(context).clear(holder.my_photo);
            Glide.with(context).clear(holder.friend_photo);

            if (Objects.equals(user_id, my_user_id)) { // 나의 채팅
                holder.my_chatting.setVisibility(View.VISIBLE);

                if (message_type == 2) {
                    holder.my_photo.setVisibility(View.VISIBLE);
                    holder.my_comment.setVisibility(View.GONE);

                    int width = cursor.getInt(cursor.getColumnIndexOrThrow("width"));
                    int height = cursor.getInt(cursor.getColumnIndexOrThrow("height"));
                    int max_width_dp = 200;
                    int max_width_px = DpToPx(context, max_width_dp);

                    ViewGroup.LayoutParams layout_params = holder.my_photo.getLayoutParams();
                    if (width > max_width_px) {
                        float ratio = (float) height / width;
                        layout_params.width = max_width_px;
                        layout_params.height = (int) (max_width_px * ratio);
                    } else {
                        layout_params.width = width;
                        layout_params.height = height;
                    }
                    holder.my_photo.setLayoutParams(layout_params);

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
                                            .into(holder.my_photo);
                                }
                            }
                        }).execute(content, "preview_" + message_id + ".jpg");
                    } else {
                        Glide.with(context)
                                .load(image_path)
                                .diskCacheStrategy(DiskCacheStrategy.ALL) // 캐싱 전략 설정
                                .override(width, height)
                                .into(holder.my_photo);
                    }

                    holder.my_photo.setOnClickListener(v -> {
                        if (photoclick != null) {
                            photoclick.onItemClick(content,message_id);
                        }
                    });
                }
                else if(message_type == 4 || message_type == 5){//파일일 경우
                    holder.my_comment.setVisibility(View.GONE);
                    holder.my_file.setVisibility(View.VISIBLE);
                    String file_name = cursor.getString(cursor.getColumnIndexOrThrow("file_name"));
                    holder.my_file_name.setText(file_name);
                    int bytes = cursor.getInt(cursor.getColumnIndexOrThrow("file_size"));
                    String file_volume;
                    if (bytes < 1024) {
                        file_volume = String.format("용량 %.2f B", (float) bytes);
                    } else if (bytes < 1024 * 1024) {
                        file_volume = String.format("용량 %.2f KB", bytes / 1024.0);
                    } else {
                        file_volume = String.format("용량 %.2f MB", bytes / (1024.0 * 1024.0));
                    }
                    holder.my_file_volume.setText(file_volume);

                    String directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                    File file = new File(directoryPath + "/ChatEast/" + file_name);

                    if(file.exists()){
                        holder.my_file_success_icon.setVisibility(View.VISIBLE);
                        holder.my_file_download_icon.setVisibility(View.GONE);
                        holder.my_file_success_icon.setOnClickListener(v -> {
                            if (fileclick != null) {
                                fileclick.onItemClick(file_name);
                            }
                        });
                    }else{
                        holder.my_file_success_icon.setVisibility(View.GONE);
                        holder.my_file_download_icon.setOnClickListener(v -> {
                            if (filedownload != null) {
                                filedownload.onItemClick(content, file_name, position);
                            }
                        });
                    }
                }
                else if(message_type==10){//보이스챗이면
                    holder.my_comment.setVisibility(View.GONE);
                    holder.my_photo.setVisibility(View.GONE);
                    holder.my_call.setVisibility(View.VISIBLE);
                    holder.my_call.setOnClickListener(v -> {
                        if (callclick != null) {
                            callclick.onItemClick(user_id,chat_db_manager.FindFriendID(my_user_id,chatroom_id));
                        }
                    });
                }
                else {//message type != 2
                    holder.my_comment.setVisibility(View.VISIBLE);
                    holder.my_photo.setVisibility(View.GONE);
                    holder.my_comment.setText(content);
                }

                holder.message_start_me.setVisibility(View.GONE);
                holder.message_time_me.setVisibility(View.GONE);
                holder.message_time_me_gone.setVisibility(View.GONE);

                if (!chat_db_manager.IsUpChatSame(user_id, chatroom_id, message_id, create_time)) { // 말풍선이 필요한 경우
                    holder.up_margin.setVisibility(View.INVISIBLE);
                    if (message_type != 2 && message_type != 10 && message_type != 4 && message_type != 5) {
                        holder.message_start_me.setVisibility(View.VISIBLE);
                    } else {
                        holder.message_start_me.setVisibility(View.GONE);
                    }
                }

                if (!chat_db_manager.IsDownChatSame(user_id, chatroom_id, message_id, create_time)) { // 시간 표시가 필요한 경우
                    holder.message_time_me.setVisibility(View.VISIBLE);

                    SimpleDateFormat inputformats = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    SimpleDateFormat outputformats = new SimpleDateFormat("a h:mm");
                    Date time = null;
                    try {
                        time = inputformats.parse(create_time);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    String formattime = outputformats.format(time);
                    holder.message_time_me.setText(formattime);
                    holder.message_time_me_gone.setVisibility(View.GONE);
                } else {
                    holder.message_time_me.setVisibility(View.GONE);
                    holder.message_time_me_gone.setVisibility(View.INVISIBLE);
                }

            } else { // 상대방 채팅
                holder.other_chatting.setVisibility(View.VISIBLE);

                if (message_type == 2) {
                    holder.friend_photo.setVisibility(View.VISIBLE);
                    holder.friend_comment.setVisibility(View.GONE);

                    int width = cursor.getInt(cursor.getColumnIndexOrThrow("width"));
                    int height = cursor.getInt(cursor.getColumnIndexOrThrow("height"));

                    int max_width_dp = 200;
                    int max_width_px = DpToPx(context, max_width_dp);

                    ViewGroup.LayoutParams layout_params = holder.friend_photo.getLayoutParams();
                    if (width > max_width_px) {
                        float ratio = (float) height / width;
                        layout_params.width = max_width_px;
                        layout_params.height = (int) (max_width_px * ratio);
                    } else {
                        layout_params.width = width;
                        layout_params.height = height;
                    }
                    holder.friend_photo.setLayoutParams(layout_params);

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
                                            .into(holder.friend_photo);
                                }
                            }
                        }).execute(content, "preview_" + message_id + ".jpg");
                    } else {
                        Glide.with(context)
                                .load(image_path)
                                .diskCacheStrategy(DiskCacheStrategy.ALL) // 캐싱 전략 설정
                                .override(width, height)
                                .into(holder.friend_photo);
                    }

                    holder.friend_photo.setOnClickListener(v -> {
                        if (photoclick != null) {
                            photoclick.onItemClick(content, message_id);
                        }
                    });

                }
                else if(message_type == 4 || message_type == 5){//파일일 경우 || 음성메세지일 경우
                    holder.my_comment.setVisibility(View.GONE);
                    holder.friend_file.setVisibility(View.VISIBLE);
                    String file_name = cursor.getString(cursor.getColumnIndexOrThrow("file_name"));
                    holder.friend_file_name.setText(file_name);
                    int bytes = cursor.getInt(cursor.getColumnIndexOrThrow("file_size"));
                    String file_volume;
                    if (bytes < 1024) {
                        file_volume = String.format("용량 %.2f B", (float) bytes);
                    } else if (bytes < 1024 * 1024) {
                        file_volume = String.format("용량 %.2f KB", bytes / 1024.0);
                    } else {
                        file_volume = String.format("용량 %.2f MB", bytes / (1024.0 * 1024.0));
                    }
                    holder.friend_file_volume.setText(file_volume);

                    String directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                    File file = new File(directoryPath + "/ChatEast/" + file_name);

                    if(file.exists()){
                        holder.friend_file_success_icon.setVisibility(View.VISIBLE);
                        holder.friend_file_download_icon.setVisibility(View.GONE);
                        holder.friend_file_success_icon.setOnClickListener(v -> {
                            if (fileclick != null) {
                                fileclick.onItemClick(file_name);
                            }
                        });
                    }else{
                        holder.friend_file_success_icon.setVisibility(View.GONE);
                        holder.friend_file_download_icon.setOnClickListener(v -> {
                            if (filedownload != null) {
                                filedownload.onItemClick(content, file_name, position);
                            }
                        });
                    }

                }
                else if(message_type==10){//보이스챗이면
                    holder.friend_comment.setVisibility(View.GONE);
                    holder.friend_photo.setVisibility(View.GONE);
                    holder.friend_call.setVisibility(View.VISIBLE);
                    holder.friend_call.setOnClickListener(v -> {
                        if (callclick != null) {
                            callclick.onItemClick(user_id,cursor.getString(cursor.getColumnIndexOrThrow("file_name")));
                        }
                    });
                }
                else {//message_type != 2
                    holder.friend_comment.setVisibility(View.VISIBLE);
                    holder.friend_photo.setVisibility(View.GONE);
                    holder.friend_comment.setText(content);
                }

                holder.friend_name.setVisibility(View.GONE);
                holder.profile_image.setVisibility(View.GONE);
                holder.message_start.setVisibility(View.GONE);
                holder.profile_image_gone.setVisibility(View.GONE);
                holder.message_time.setVisibility(View.GONE);
                holder.message_time_gone.setVisibility(View.GONE);
                if (!chat_db_manager.IsUpChatSame(user_id, chatroom_id, message_id, create_time)) { // 말풍선과 프로필이 필요한 경우
                    holder.up_margin.setVisibility(View.INVISIBLE);
                    holder.friend_name.setVisibility(View.VISIBLE);
                    holder.friend_name.setText(chat_db_manager.GetNickname(my_user_id,user_id));
                    holder.profile_image.setVisibility(View.VISIBLE);
                    holder.profile_image.setOnClickListener(v -> {
                        if (on_item_click_listener != null) {
                            on_item_click_listener.onItemClick(user_id);
                        }
                    });
                    if (message_type != 2) {
                        holder.message_start.setVisibility(View.VISIBLE);
                    } else {
                        holder.message_start.setVisibility(View.GONE);
                    }

                    String profile_img_url = chat_db_manager.GetProfileImg(user_id);

                    if(!Objects.equals(profile_img_url, "null")) {
                        @SuppressLint("SdCardPath")
                        String image_path = "/data/data/com.example.chat_east/cache/C_E_preview/preview_" + ExtractFileNameFromURL(profile_img_url) + ".jpg";
                        File image_file = new File(image_path);

                        if (!image_file.exists()) {
                            new save_image_task_profile(context, profile_img_url, new save_image_task_profile.SaveImageCallback() {
                                @Override
                                public void Onimagesaved(String profile_img_urls, String image_path) {
                                    if (profile_img_url == profile_img_urls) {
                                        Glide.with(context)
                                                .load(image_path)
                                                .diskCacheStrategy(DiskCacheStrategy.ALL) // 캐싱 전략 설정
                                                .into(holder.profile_image);
                                    }
                                }
                            }).execute(profile_img_url, "preview_" + ExtractFileNameFromURL(profile_img_url) + ".jpg");
                        } else {
                            Glide.with(context)
                                    .load(image_path)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL) // 캐싱 전략 설정
                                    .into(holder.profile_image);
                        }
                    }


                } else {
                    holder.profile_image_gone.setVisibility(View.INVISIBLE);
                }
                if (!chat_db_manager.IsDownChatSame(user_id, chatroom_id, message_id, create_time)) { // 시간 표시가 필요한 경우
                    holder.message_time.setVisibility(View.VISIBLE);
                    SimpleDateFormat input_formats = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    SimpleDateFormat output_formats = new SimpleDateFormat("a h:mm");
                    Date time = null;
                    try {
                        time = input_formats.parse(create_time);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    String format_time = output_formats.format(time);
                    holder.message_time.setText(format_time);
                    holder.message_time_gone.setVisibility(View.GONE);
                } else {
                    holder.message_time.setVisibility(View.GONE);
                    holder.message_time_gone.setVisibility(View.INVISIBLE);
                }
            }

            holder.profile_image.setOnClickListener(v -> {
                if (on_item_click_listener != null) {
                    on_item_click_listener.onItemClick(user_id);
                }
            });
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

    public void AddMessages(Cursor new_cursor) {
        if (new_cursor != null && new_cursor.getCount() > 0) {
            int old_count = cursor.getCount();
            MatrixCursor matrix_cursor = new MatrixCursor(cursor.getColumnNames());

            if (cursor.moveToFirst()) {
                do {
                    Object[] row_values = new Object[cursor.getColumnCount()];
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        row_values[i] = cursor.getString(i);
                    }
                    matrix_cursor.addRow(row_values);
                } while (cursor.moveToNext());
            }
            if (new_cursor.moveToFirst()) {
                do {
                    Object[] row_values = new Object[new_cursor.getColumnCount()];
                    for (int i = 0; i < new_cursor.getColumnCount(); i++) {
                        row_values[i] = new_cursor.getString(i);
                    }
                    matrix_cursor.addRow(row_values);
                } while (new_cursor.moveToNext());
            }
            SwapCursor(matrix_cursor);
            notifyItemRangeInserted(old_count, new_cursor.getCount());
        }
    }


    static class Messagelistvh extends RecyclerView.ViewHolder {
        View up_margin;
        TextView chatting_date, friend_name, friend_comment, message_time, message_time_gone, message_time_me, message_time_me_gone, my_comment, friend_file_name, friend_file_volume, my_file_name, my_file_volume, voice_text;
        LinearLayout other_chatting, my_chatting, friend_file, my_file, friend_call, my_call;
        ImageView profile_image, profile_image_gone, message_start, message_start_me, friend_photo, my_photo;
        CardView friend_file_download_icon, friend_file_success_icon, my_file_download_icon, my_file_success_icon;
        public Messagelistvh(@NonNull View itemview) {
            super(itemview);
            up_margin = itemview.findViewById(R.id.up_margin);
            chatting_date = itemview.findViewById(R.id.chatting_date);
            other_chatting = itemview.findViewById(R.id.other_chatting);
            profile_image = itemview.findViewById(R.id.profile_image);
            profile_image_gone = itemview.findViewById(R.id.profile_image_gone);
            friend_name = itemview.findViewById(R.id.friend_name);
            message_start = itemview.findViewById(R.id.message_start);
            friend_comment = itemview.findViewById(R.id.friend_comment);
            message_time = itemview.findViewById(R.id.message_time);
            message_time_gone = itemview.findViewById(R.id.message_time_gone);
            my_chatting = itemview.findViewById(R.id.my_chatting);
            message_time_me = itemview.findViewById(R.id.message_time_me);
            message_time_me_gone = itemview.findViewById(R.id.message_time_me_gone);
            my_comment = itemview.findViewById(R.id.my_comment);
            message_start_me = itemview.findViewById(R.id.message_start_me);
            friend_photo = itemview.findViewById(R.id.friend_photos);
            my_photo = itemview.findViewById(R.id.my_photos);
            friend_file = itemview.findViewById(R.id.friend_file);
            friend_file_name = itemview.findViewById(R.id.friend_file_name);
            friend_file_volume = itemview.findViewById(R.id.friend_file_volume);
            friend_file_download_icon = itemview.findViewById(R.id.friend_file_download_icon);
            friend_file_success_icon = itemview.findViewById(R.id.friend_file_success_icon);
            my_file = itemview.findViewById(R.id.my_file);
            my_file_name = itemview.findViewById(R.id.my_file_name);
            my_file_volume = itemview.findViewById(R.id.my_file_volume);
            my_file_download_icon = itemview.findViewById(R.id.my_file_download_icon);
            my_file_success_icon = itemview.findViewById(R.id.my_file_success_icon);
            friend_call = itemview.findViewById(R.id.friend_call);
            my_call = itemview.findViewById(R.id.my_call);
            voice_text = itemview.findViewById(R.id.voice_text);
        }
    }

    public void SwapCursor(Cursor newcursor) {
        if (cursor != null) {
            cursor.close();
        }
        cursor = newcursor;
        if (newcursor != null) {
            notifyDataSetChanged();
        }
    }

    public void close() {
        if (chat_db_manager != null) {
            chat_db_manager.close();
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    private int DpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
