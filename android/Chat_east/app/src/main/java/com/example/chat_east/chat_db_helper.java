package com.example.chat_east;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class chat_db_helper extends SQLiteOpenHelper {

    private static final String db_name = "chat.db";
    private static final int db_version = 1;

    public chat_db_helper(Context context) {
        super(context, db_name, null, db_version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE messages (user_id TEXT, message_id INT, chatroom_id INT, content TEXT, file_name TEXT, message_type INT,width INT, height INT, file_size INT, create_time TEXT, update_time TEXT)");
        db.execSQL("CREATE TABLE chatroom_list (user_id TEXT, chatroom_id INT, chatroom_name TEXT, chatroom_type INT, favorite_type, notification_type INT, chatroom_img_url TEXT, update_time TEXT, people_count INT, last_message TEXT, last_message_time TEXT, last_view_time TEXT)");
        db.execSQL("CREATE TABLE chatroom_user (my_user_id TEXT, friend_user_id TEXT, nickname TEXT, chatroom_id INT, friends_type INT, join_type INT, comment TEXT, profile_img_url TEXT, background_img_url TEXT, update_time TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int old_version, int new_version) {
        db.execSQL("DROP TABLE IF EXISTS messages");
        onCreate(db);
    }
}
