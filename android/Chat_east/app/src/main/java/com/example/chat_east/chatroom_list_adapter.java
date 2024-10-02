package com.example.chat_east;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import android.widget.ImageView;

public class chatroom_list_adapter extends RecyclerView.Adapter<chatroom_list_adapter.Chatroomlistvh> {
    private chat_db_manager chat_db_manager;
    private friend_db_manager friend_db_manager;
    private Cursor cursor;
    private Context context;
    private String my_user_id;
    private chatroom_list_adapter.OnItemClickListener on_item_click_listener;

    public interface OnItemClickListener {
        void onItemClick(int chatroom_id);
    }

    public chatroom_list_adapter(Context context, String my_user_id, Cursor cursor, chatroom_list_adapter.OnItemClickListener onitemclicklistener) {
        this.context = context;
        this.cursor = cursor;
        this.on_item_click_listener = onitemclicklistener;
        chat_db_manager = new chat_db_manager(context);
        friend_db_manager = new friend_db_manager(context);
        this.my_user_id = my_user_id;
    }

    @NonNull
    @Override
    public Chatroomlistvh onCreateViewHolder(@NonNull ViewGroup parent, int viewtype) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatroom_list, parent, false);
        return new Chatroomlistvh(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Chatroomlistvh holder, int position) {
        if (cursor.moveToPosition(position)) {
            String user_id = cursor.getString(cursor.getColumnIndexOrThrow("user_id"));
            String chatroom_image_url = cursor.getString(cursor.getColumnIndexOrThrow("chatroom_img_url"));
            String chatroom_name = cursor.getString(cursor.getColumnIndexOrThrow("chatroom_name"));
            int people_count = cursor.getInt(cursor.getColumnIndexOrThrow("people_count"));
            String last_message = cursor.getString(cursor.getColumnIndexOrThrow("last_message"));
            String last_message_time = cursor.getString(cursor.getColumnIndexOrThrow("last_message_time"));
            int chatroom_id = cursor.getInt(cursor.getColumnIndexOrThrow("chatroom_id"));
            int message_count = chat_db_manager.GetLastMessageCount(user_id, chatroom_id);

            int chatroom_type = chat_db_manager.GetChatroomType(my_user_id,chatroom_id);
            String my_nickname = friend_db_manager.GetMyNickname(my_user_id);

            holder.people_count.setVisibility(View.GONE);

            if (Objects.equals(last_message_time, "null")) {
                last_message_time = "";
            } else {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date message_date = sdf.parse(last_message_time);
                    Calendar message_cal = Calendar.getInstance();
                    message_cal.setTime(message_date);

                    Calendar today_cal = Calendar.getInstance();
                    today_cal.set(Calendar.HOUR_OF_DAY, 0);
                    today_cal.set(Calendar.MINUTE, 0);
                    today_cal.set(Calendar.SECOND, 0);
                    today_cal.set(Calendar.MILLISECOND, 0);

                    long diff = today_cal.getTimeInMillis() - message_cal.getTimeInMillis();
                    long days_diff = diff / (24 * 60 * 60 * 1000);

                    if (days_diff == 0) {
                        SimpleDateFormat time_format = new SimpleDateFormat("a h:mm");
                        last_message_time = time_format.format(message_date);
                    } else if (days_diff == 1) {
                        last_message_time = "어제";
                    } else {
                        SimpleDateFormat date_format = new SimpleDateFormat("M월 d일");
                        last_message_time = date_format.format(message_date);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    last_message_time = "";
                }
            }
            if (Objects.equals(last_message, "null"))last_message ="";

            if(chatroom_type==1 || chatroom_type == 3){
                holder.chatroom_name.setText(chatroom_name);
            }else{
                String[] names = chatroom_name.split(", ");
                StringBuilder new_chatroom_name = new StringBuilder();
                for (String name : names) {
                    if (!name.equals(my_nickname)) {
                        if (new_chatroom_name.length() > 0) {
                            new_chatroom_name.append(", ");
                        }
                        new_chatroom_name.append(name);
                    }
                }
                holder.chatroom_name.setText(new_chatroom_name.toString());
            }

            if(holder.chatroom_name.length()>20){
                String new_chatroom_name = holder.chatroom_name.getText().toString();
                new_chatroom_name = new_chatroom_name.substring(0,20) + "...";
                holder.chatroom_name.setText(new_chatroom_name);
            }

            if(chatroom_type==3 || chatroom_type==4){
                holder.people_count.setVisibility(View.VISIBLE);
                holder.people_count.setText(String.valueOf(people_count));
            }
            holder.chatroom_last_comment.setText(last_message);
            holder.chatroom_last_time.setText(last_message_time);

            //pill
            if(!Objects.equals(chatroom_image_url, "null")) {
                Glide.with(context)
                        .load(chatroom_image_url)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into((ImageView) holder.chatroom_img);
            }
            //pill

            holder.itemView.setOnClickListener(v -> {
                if (on_item_click_listener != null) {
                    on_item_click_listener.onItemClick(chatroom_id);
                }
            });
            if(message_count<1){
                holder.unread_count.setVisibility(View.INVISIBLE);
            }else if(message_count>99){
                holder.unread_count.setVisibility(View.VISIBLE);
                holder.unread_count.setText("99+");
            }else{
                holder.unread_count.setVisibility(View.VISIBLE);
                holder.unread_count.setText(String.valueOf(message_count));
            }

        }
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    static class Chatroomlistvh extends RecyclerView.ViewHolder {
        View chatroom_img;
        TextView chatroom_name, chatroom_last_time, chatroom_last_comment, unread_count, people_count;
        public Chatroomlistvh(@NonNull View itemview) {
            super(itemview);
            chatroom_img = itemview.findViewById(R.id.chatroom_image);
            chatroom_name = itemview.findViewById(R.id.chatroom_name);
            people_count = itemview.findViewById(R.id.people_count);
            chatroom_last_time = itemview.findViewById(R.id.chatroom_last_time);
            chatroom_last_comment = itemview.findViewById(R.id.chatroom_last_comment);
            unread_count = itemview.findViewById(R.id.unread_count);
        }
    }

    public void Swapcursor(Cursor newcursor) {
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
    }

}
