package com.example.chat_east;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

public class friend_db_manager {

    private friend_db_helper dbhelper;
    private SQLiteDatabase database;
    private SimpleDateFormat sdf;
    private chat_db_manager chat_db_manager;

    public friend_db_manager(Context context) {
        dbhelper = new friend_db_helper(context);
        chat_db_manager = new chat_db_manager(context);
        database = dbhelper.getWritableDatabase();
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
    }


    public void FriendUpdate(String my_user_id, String friend_user_id, String nickname, String comment, int friend_type, String profile_img_url, String background_img_url, String update_time) {
        ContentValues values = new ContentValues();

        values.put("my_user_id", my_user_id);
        values.put("friend_user_id", friend_user_id);
        values.put("nickname", nickname);
        values.put("comment", comment);
        values.put("friend_type", friend_type);
        values.put("profile_img_url", profile_img_url);
        values.put("background_img_url", background_img_url);
        values.put("update_time",sdf.format(new Date()));

        if(DBUserIdCheck(my_user_id,friend_user_id)){
            database.update("friend", values, "my_user_id = " + my_user_id + " and friend_user_id = " + friend_user_id, null);
        }else{
            database.insert("friend", null, values);
        }

        chat_db_manager.ChangeFriendType(my_user_id, friend_user_id, friend_type);

    }

    public void SaveProfile(String user_id, String nickname, String comment, String profile_img_url, String background_img_url, String update_time) {
        ContentValues values = new ContentValues();

        values.put("user_id", user_id);
        values.put("nickname", nickname);
        values.put("comment", comment);
        values.put("profile_img_url", profile_img_url);
        values.put("background_img_url", background_img_url);
        values.put("update_time",update_time);

        if(DBProFileCheck(user_id)){
            database.update("profile", values, "user_id = " + user_id, null);
        }else{
            database.insert("profile", null, values);
        }

    }

    public void IgnoreDelete(String my_user_id, String friend_user_id){
        ContentValues values = new ContentValues();
        values.put("friend_type",0);
        database.update("friend",values,"my_user_id = " + my_user_id + " and friend_user_id = " + friend_user_id,null);
    }

    public Cursor ShowFriend(String my_user_id) {
        return database.query("friend", null, "my_user_id = " + my_user_id + " and (friend_type = 1 or friend_type = 2)", null, null, null, null);
    }

    public Cursor ShowProfile(String user_id) {
        return database.query("profile", null, "user_id = " + user_id, null, null, null, null);
    }


    public void ChangeNickname(String user_id, String nickname){
        ContentValues values = new ContentValues();
        values.put("nickname",nickname);
        database.update("profile", values, "user_id = " + user_id, null);
    }

    public void ChangeComment(String user_id, String comment){
        ContentValues values = new ContentValues();
        values.put("comment",comment);
        database.update("profile", values, "user_id = " + user_id, null);
    }

    public void ChangeProfileImg(String user_id, String profile_img_url){
        ContentValues values = new ContentValues();
        values.put("profile_img_url",profile_img_url);
        database.update("profile", values, "user_id = " + user_id, null);
    }

    public void ChangeBackgroundImg(String user_id, String background_img_url){
        ContentValues values = new ContentValues();
        values.put("background_img_url",background_img_url);
        database.update("profile", values, "user_id = " + user_id, null);
    }

    public Cursor GetLastTime(String my_user_id) {//데이터 마지막 저장시간 불러오기
        return database.query("friend",new String[]{"update_time"}, "my_user_id = " + my_user_id, null, null, null, "update_time DESC", "1");
    }
    public String GetNickname(String my_user_id, String friend_user_id) {
        Cursor cursor = database.query("friend",new String[]{"nickname"}, "my_user_id = " + my_user_id + " and friend_user_id = " + friend_user_id, null, null, null, "update_time DESC", "1");
        if(cursor.getCount()>0){
            cursor.moveToFirst();
            int a =cursor.getColumnIndex("nickname");
            String nickname = cursor.getString(a);
            cursor.close();
            return nickname;
        }
        cursor.close();
        return "";
    }

    public String GetProfileImg(String my_user_id,String friend_user_id) {
        Cursor cursor = database.query("friend",new String[]{"profile_img_url"}, "my_user_id = " + my_user_id + " and friend_user_id = " + friend_user_id, null, null, null, "update_time DESC", "1");
        if(cursor.getCount()>0){
            cursor.moveToFirst();
            int a =cursor.getColumnIndex("profile_img_url");
            String profile_img_url = cursor.getString(a);
            cursor.close();
            return profile_img_url;
        }
        cursor.close();
        return "";
    }

    public String GetMyNickname(String my_user_id) {//데이터 마지막 저장시간 불러오기
        Cursor cursor = database.query("profile",new String[]{"nickname"}, "user_id = " + my_user_id, null, null, null, "update_time DESC", "1");
        if(cursor.getCount()>0){
            cursor.moveToFirst();
            int a =cursor.getColumnIndex("nickname");
            String nickname = cursor.getString(a);
            cursor.close();
            return nickname;
        }
        cursor.close();
        return "";
    }

    public String GetMyProfileUrl(String my_user_id) {//데이터 마지막 저장시간 불러오기
        Cursor cursor = database.query("profile",new String[]{"profile_img_url"}, "user_id = " + my_user_id, null, null, null, "update_time DESC", "1");
        if(cursor.getCount()>0){
            cursor.moveToFirst();
            int a =cursor.getColumnIndex("profile_img_url");
            String profile_img_url = cursor.getString(a);
            cursor.close();
            return profile_img_url;
        }
        cursor.close();
        return "";
    }

    public Cursor GetProfileTime(String user_id) {//데이터 마지막 저장시간 불러오기
        return database.query("profile",new String[]{"update_time"}, "user_id = " + user_id, null, null, null, "update_time DESC", "1");
    }

    public boolean DBUserIdCheck(String my_user_id, String friend_user_id) {
        Cursor cursor = database.query("friend", new String[]{"friend_user_id"}, "my_user_id = "+ my_user_id + " and friend_user_id = " + friend_user_id, null, null, null, null);
        if(cursor.getCount()>0){
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    public boolean DBProFileCheck(String user_id) {
        Cursor cursor = database.query("profile", new String[]{"user_id"}, "user_id = "+ user_id, null, null, null, null);
        if(cursor.getCount()>0){
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    public boolean IsFriend(String my_user_id, String friend_user_id) {
        Cursor cursor = database.query("friend", new String[]{"friend_user_id"}, "my_user_id = "+ my_user_id + " and friend_user_id = " + friend_user_id + " and (friend_type = 1 or friend_type = 2)", null, null, null, null);
        if(cursor.getCount()>0){
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    public boolean FindIgnoreUserID(String my_user_id, String friend_user_id) {
        Cursor cursor = database.query("friend", new String[]{"friend_user_id"}, "my_user_id = " + my_user_id + " and friend_user_id = " + friend_user_id + " and friend_type = 3", null, null, null, null);
        if(cursor.getCount()>0){
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    public String FriendTypeCheck(String my_user_id, String friend_user_id){
        if(Objects.equals(my_user_id, friend_user_id))return "-1";
        Cursor cursor = database.query("friend", new String[]{"friend_type"}, "my_user_id = " + my_user_id + " and friend_user_id = " + friend_user_id, null, null, null, null);
        if(cursor.getCount()>0){
            cursor.moveToFirst();
            int a =cursor.getColumnIndex("friend_type");
            String friend_type = cursor.getString(a);
            cursor.close();
            return friend_type;
        }
        cursor.close();
        return "0";
    }


    public void close() {
        dbhelper.close();
    }
}