package com.example.chat_east;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class friend_db_helper extends SQLiteOpenHelper {

    private static final String db_name = "friend.db";
    private static final int db_version = 1;

    public friend_db_helper(Context context) {
        super(context, db_name, null, db_version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE friend (my_user_id TEXT, friend_user_id TEXT, nickname TEXT, comment TEXT, friend_type int, profile_img_url TEXT,background_img_url TEXT, update_time TEXT)");
        db.execSQL("CREATE TABLE profile (user_id TEXT, nickname TEXT, comment TEXT, profile_img_url TEXT,background_img_url TEXT, update_time TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int old_version, int new_version) {
        db.execSQL("DROP TABLE IF EXISTS messages");
        onCreate(db);
    }

}
