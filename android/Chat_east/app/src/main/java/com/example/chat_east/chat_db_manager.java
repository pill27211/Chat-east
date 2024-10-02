package com.example.chat_east;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

public class chat_db_manager {

    private chat_db_helper db_helper;
    private SQLiteDatabase database;
    private SimpleDateFormat sdf;
    Context context;

    public chat_db_manager(Context context) {
        db_helper = new chat_db_helper(context);
        database = db_helper.getWritableDatabase();
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        this.context = context;
    }

    public void AddMessage(String user_id, int message_id, int chatroom_id, String content, String file_name, int message_type, int width, int height, int file_size, String create_time, String update_time) {
        ContentValues values = new ContentValues();

        values.put("user_id", user_id);
        values.put("message_id", message_id);
        values.put("chatroom_id",chatroom_id);
        values.put("content", content);
        values.put("file_name",file_name);
        values.put("message_type",message_type);
        values.put("width",width);
        values.put("height",height);
        values.put("file_size",file_size);
        values.put("create_time",create_time);
        values.put("update_time",update_time);

        if(MessageCheck(message_id))return;

        database.insert("messages", null, values);
    }

    public boolean MessageCheck(int message_id){
        Cursor cursor = database.query("messages", null,"message_id = " + message_id, null, null, null, "message_id DESC", "1");
        return cursor.getCount() > 0;
    }

    public Cursor GetMessage(int message_id){
        Cursor cursor = database.query("messages",null, "message_id = " + message_id, null, null, null, null, "1");
        return cursor;
    }

    public Cursor GetImg(int chatroom_id){
        Cursor cursor = database.query("messages",null, "chatroom_id = " + chatroom_id + " and message_type = 2", null, null, null, "create_time DESC");
        return cursor;
    }

    public Cursor GetLastFourImage(int chatroom_id){
        Cursor cursor = database.query("messages",null, "message_type = 2 and chatroom_id = "+ chatroom_id, null, null, null, "create_time DESC", "4");
        return cursor;
    }

    public void DeleteFakeMessage(int chatroom_id){
        database.delete("messages","chatroom_id = " + chatroom_id + " and message_type = -1",null);
    }

    public Cursor GetLastTime() {//데이터 마지막 저장시간 불러오기
        return database.query("messages",new String[]{"create_time"}, null, null, null, null, "create_time DESC", "1");
    }

    public Cursor GetDBMessage(int chatroom_id){//채팅방 번호에 해당하는 채팅 불러오기
        return database.query("messages", null, "chatroom_id = "+chatroom_id, null, null, null, "create_time ASC");
    }

    public Cursor GetMessageById(int message_id){
        return database.query("messages", null, "message_id = " + message_id, null, null, null, null,"1");
    }

    public int GetLastMessageCount(String user_id, int chatroom_id){
        Cursor cursor = database.query("messages", null, " chatroom_id = " + chatroom_id + " and create_time > '" + GetLastViewTime(user_id, chatroom_id) + "'", null, null, null, null);
        int max_count = Math.max(cursor.getCount(), 0);
        cursor.close();
        return max_count;
    }

    public boolean IsUpChatSame(String user_id, int chatroom_id, int message_id, String create_time){//조건 만족하면 말풍선 없음
        Cursor cursor = database.query("messages", null,"chatroom_id = " + chatroom_id + " and message_id < " + message_id, null, null, null, "message_id DESC", "1");
        if(cursor.getCount()>0){
            cursor.moveToFirst();
            int a =cursor.getColumnIndex("user_id");
            int b =cursor.getColumnIndex("create_time");
            if(Objects.equals(cursor.getString(a), user_id) && Objects.equals(cursor.getString(b).substring(0,16), create_time.substring(0,16))){
                cursor.close();
                return true;
            }
            else {
                cursor.close();
                return false;
            }
        }
        cursor.close();
        return false;
    }

    public boolean IsDownChatSame(String user_id, int chatroom_id, int message_id, String create_time){//조건 만족하면 시간없음
        Cursor cursor = database.query("messages", null,"chatroom_id = " + chatroom_id + " and message_id > " + message_id, null, null, null, "message_id ASC", "1");
        if(cursor.getCount()>0){
            cursor.moveToFirst();
            int a =cursor.getColumnIndex("user_id");
            int b =cursor.getColumnIndex("create_time");
            if(Objects.equals(cursor.getString(a), user_id) && Objects.equals(cursor.getString(b).substring(0,16), create_time.substring(0,16))){
                cursor.close();
                return true;
            }
            else {
                cursor.close();
                return false;
            }
        }
        cursor.close();
        return false;
    }

    public boolean IsDateChange(int chatroom_id, int message_id, String create_time){
        Cursor cursor = database.query("messages", null,"chatroom_id = " + chatroom_id + " and message_id < " + message_id, null, null, null, "message_id DESC", "1");
        if(cursor.getCount()>0){
            cursor.moveToFirst();
            int a =cursor.getColumnIndex("create_time");
            if(Objects.equals(cursor.getString(a).substring(0,10), create_time.substring(0,10))){
                cursor.close();
                return false;
            }
            else {
                cursor.close();
                return true;
            }
        }
        cursor.close();
        return true;
    }

    public String GetChatroomLastTime(int chatroom_id){
        Cursor cursor = database.query("messages", new String[]{"create_time"}, "chatroom_id = " + chatroom_id, null, null, null, "create_time DESC", "1");
        if(cursor.getCount()>0){
            cursor.moveToFirst();
            int a =cursor.getColumnIndex("create_time");
            String last_time = cursor.getString(a);
            cursor.close();
            return last_time;
        }
        cursor.close();
        return "2020-01-01 00:00:00";
    }

    public int GetSmallMessageID(int chatroom_id){
        Cursor cursor = database.query("messages", new String[]{"message_id"}, null, null, null, null, "message_id ASC", "1");
        if(cursor.getCount()>0){
            cursor.moveToFirst();
            int a =cursor.getColumnIndex("message_id");
            int small_message_id = cursor.getInt(a);
            cursor.close();
            return small_message_id;
        }
        cursor.close();
        return 0;
    }

    public void AddChatroomList(String user_id, int chatroom_id, String chatroom_name, int chatroom_type, int favorite_type, int notification_type, String chatroom_img_url, String update_time, int people_count, String last_message, String last_message_time, String last_view_time){
        ContentValues values = new ContentValues();

        values.put("user_id", user_id);
        values.put("chatroom_id",chatroom_id);
        values.put("chatroom_name",chatroom_name);
        values.put("chatroom_type",chatroom_type);
        values.put("favorite_type",favorite_type);
        values.put("notification_type",notification_type);
        values.put("chatroom_img_url",chatroom_img_url);
        if(!Objects.equals(update_time, "2020-01-01 00:00:00")){
            values.put("update_time",sdf.format(new Date()));
        }else{
            values.put("update_time",update_time);
        }
        values.put("people_count",people_count);
        values.put("last_message",last_message);
        values.put("last_message_time",last_message_time);
        if(Objects.equals(last_view_time, "null")){
            values.put("last_view_time","2020-01-01 00:00:00");
        }else{
            values.put("last_view_time",last_view_time);
        }

        if(ChatroomJoinCheck(user_id, chatroom_id)){
            database.update("chatroom_list", values, "user_id = " + user_id + " and chatroom_id = " + chatroom_id, null);
        }else {
            database.insert("chatroom_list", null, values);
        }
    }

    public boolean ChatroomJoinCheck(String user_id, int chatroom_id){
        Cursor cursor = database.query("chatroom_list", null, "user_id = " + user_id + " and chatroom_id = " + chatroom_id, null, null, null, null);
        if(cursor.getCount()>0){
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    public String GetListLastTime(String user_id){
        Cursor cursor = database.query("chatroom_list", new String[]{"update_time"}, "user_id = " + user_id, null, null, null, "update_time DESC", "1");
        if(cursor.getCount()>0){
            cursor.moveToFirst();
            int a =cursor.getColumnIndex("update_time");
            String last_time = cursor.getString(a);
            cursor.close();
            return last_time;
        }
        cursor.close();
        return "2020-01-01 00:00:00";
    }

    public String GetLastViewTime(String user_id, int chatroom_id){
        Cursor cursor = database.query("chatroom_list", new String[]{"last_view_time"}, "user_id = " + user_id + " and chatroom_id = " + chatroom_id, null, null, null, "last_view_time DESC", "1");
        if(cursor.getCount()>0){
            cursor.moveToFirst();
            int a =cursor.getColumnIndex("last_view_time");
            String last_view_time = cursor.getString(a);
            cursor.close();
            return last_view_time;
        }
        cursor.close();
        return "2020-01-01 00:00:00";
    }

    public Cursor ShowChatroomList(String user_id) {
        return database.query("chatroom_list", null, "user_id = " + user_id + " and favorite_type != 0 and last_message_time != \'null\'", null, null, null, "last_message_time DESC");
    }

    public void LeaveChatroom(String user_id, int chatroom_id){
        ContentValues values = new ContentValues();
        values.put("favorite_type",0);
        database.update("chatroom_list", values, "user_id = " + user_id + " and chatroom_id = " + chatroom_id, null);
    }

    public int GetPeopleCount(int chatroom_id){
        Cursor cursor = database.query("chatroom_list", new String[]{"people_count"}, "chatroom_id = " + chatroom_id, null, null, null, null);
        cursor.moveToFirst();
        int a =cursor.getColumnIndex("people_count");
        int people_count = cursor.getInt(a);
        cursor.close();
        return people_count;
    }

    public int GetChatroomType(String user_id, int chatroom_id){
        Cursor cursor = database.query("chatroom_list", new String[]{"chatroom_type"}, "user_id = " + user_id + " and chatroom_id = " + chatroom_id, null, null, null, null);
        cursor.moveToFirst();
        int a =cursor.getColumnIndex("chatroom_type");
        int chatroom_type = cursor.getInt(a);
        cursor.close();
        return chatroom_type;
    }

    public String GetChatroomName(int chatroom_id){
        Cursor cursor = database.query("chatroom_list", new String[]{"chatroom_name"}, "chatroom_id = " + chatroom_id, null, null, null, null);
        cursor.moveToFirst();
        int a =cursor.getColumnIndex("chatroom_name");
        String chatroom_name = cursor.getString(a);
        cursor.close();
        return chatroom_name;
    }

    public int GetNotificationType(int chatroom_id){
        Cursor cursor = database.query("chatroom_list", new String[]{"notification_type"}, "chatroom_id = " + chatroom_id, null, null, null, null);
        cursor.moveToFirst();
        int a =cursor.getColumnIndex("notification_type");
        int notification_type = cursor.getInt(a);
        cursor.close();
        return notification_type;
    }

    public int GetChatroomType(int chatroom_id){
        Cursor cursor = database.query("chatroom_list", new String[]{"chatroom_type"}, "chatroom_id = " + chatroom_id, null, null, null, null);
        cursor.moveToFirst();
        int a =cursor.getColumnIndex("chatroom_type");
        int chatroom_type = cursor.getInt(a);
        cursor.close();
        return chatroom_type;
    }

    public boolean IsChatroomExist(String my_user_id, int chatroom_id){
        Cursor cursor = database.query("chatroom_list", null, "user_id = " + my_user_id + " and chatroom_id = " + chatroom_id, null, null, null, null);
        if(cursor.getCount()>0){
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    public int GetFavoriteType(int chatroom_id){
        Cursor cursor = database.query("chatroom_list", new String[]{"favorite_type"}, "chatroom_id = " + chatroom_id, null, null, null, null);
        cursor.moveToFirst();
        int a =cursor.getColumnIndex("favorite_type");
        int favorite_type = cursor.getInt(a);
        cursor.close();
        return favorite_type;
    }

    public void UpdateNotificationType(String my_user_id, int chatroom_id, int notification_type){
        ContentValues values = new ContentValues();

        values.put("notification_type",notification_type);

        database.update("chatroom_list",values,"user_id = " + my_user_id + " and chatroom_id = " + chatroom_id,null);

    }

    public void UpdateFavoriteType(String my_user_id, int chatroom_id, int favorite_type){
        ContentValues values = new ContentValues();
        values.put("favorite_type",favorite_type);
        database.update("chatroom_list",values,"user_id = " + my_user_id + " and chatroom_id = " + chatroom_id,null);
    }


    public void UpdateChatroomType(String my_user_id, int chatroom_id, int chatroom_type){
        ContentValues values = new ContentValues();

        values.put("chatroom_type",chatroom_type);

        database.update("chatroom_list",values,"user_id = " + my_user_id + " and chatroom_id = " + chatroom_id,null);

    }

    public String FindFriendID(String my_user_id, int chatroom_id){
        Cursor cursor = database.query("chatroom_user", new String[]{"friend_user_id"}, "my_user_id = " + my_user_id + " and chatroom_id = " + chatroom_id, null, null, null, null);
        cursor.moveToFirst();
        int a =cursor.getColumnIndex("friend_user_id");
        String friend_id = cursor.getString(a);
        cursor.close();
        return friend_id;
    }

    public void AddChatroomUser(String my_user_id, String user_id, String nickname, int chatroom_id, int friends_type, int join_type, String comment, String profile_img_url, String background_img_url, String update_time){
        ContentValues values = new ContentValues();

        values.put("my_user_id",my_user_id);
        values.put("friend_user_id", user_id);
        values.put("nickname", nickname);
        values.put("chatroom_id",chatroom_id);
        values.put("friends_type",friends_type);
        values.put("join_type",join_type);
        values.put("comment",comment);
        values.put("profile_img_url",profile_img_url);
        values.put("background_img_url",background_img_url);
        values.put("update_time",update_time);

        if(ChatroomUserCheck(my_user_id, user_id, chatroom_id)){
            database.update("chatroom_user", values, "my_user_id = " + my_user_id + " and friend_user_id = " + user_id + " and chatroom_id = " + chatroom_id, null);
        }else {
            database.insert("chatroom_user", null, values);
        }
    }

    public boolean ChatroomUserCheck(String my_user_id, String friend_user_id, int chatroom_id){
        Cursor cursor = database.query("chatroom_user", null, "my_user_id = " + my_user_id + " and friend_user_id = " + friend_user_id + " and chatroom_id = " + chatroom_id, null, null, null, null);
        if(cursor.getCount()>0){
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    public Cursor ChatroomCursor(String my_user_id, int chatroom_id){
        Cursor cursor = database.query("chatroom_user", null, "my_user_id = " + my_user_id + " and chatroom_id = " + chatroom_id + " and join_type != 0 and friends_type != 0", null, null, null, null);
        return cursor;
    }

    public String GetLastUserTime(String user_id, int chatroom_id){
        Cursor cursor = database.query("chatroom_user", new String[]{"update_time"}, "my_user_id = " +user_id + " and chatroom_id = " + chatroom_id, null, null, null, "update_time DESC", "1");
        if(cursor.getCount()>0){
            cursor.moveToFirst();
            int a =cursor.getColumnIndex("update_time");
            String last_user_time = cursor.getString(a);
            cursor.close();
            return last_user_time;
        }
        return "2020-01-01 00:00:00";
    }

    public String GetProfileImg(String user_id){
        Cursor cursor = database.query("chatroom_user", new String[]{"profile_img_url"}, "friend_user_id = " +user_id, null, null, null, "update_time DESC", "1");
        if(cursor.getCount()>0){
            cursor.moveToFirst();
            int a =cursor.getColumnIndex("profile_img_url");
            String profile_img_url = cursor.getString(a);
            cursor.close();
            return profile_img_url;
        }
        return "null";
    }

    public Cursor GetFriendInfo(String my_user_id, String friend_user_id){
        Cursor cursor = database.query("chatroom_user", null, "my_user_id = " + my_user_id + " and friend_user_id = " + friend_user_id, null, null, null, "update_time DESC", "1");
        return cursor;
    }

    public void ChangeFriendType(String my_user_id, String friend_user_id, int friends_type){
        ContentValues values = new ContentValues();
        values.put("friends_type",friends_type);

        if(IsFriendInChatroomUser(my_user_id, friend_user_id)){//chatroom_user 에 해당 user_id 를 가진 친구가 있을시 friends_type 을 교체
            database.update("chatroom_user",values,"my_user_id = " + my_user_id + " and friend_user_id = " + friend_user_id, null);
        }
    }

    public boolean IsFriendInChatroomUser(String my_user_id, String friend_user_id){
        Cursor cursor = database.query("chatroom_user", null, "my_user_id = " + my_user_id + " and friend_user_id = " + friend_user_id, null, null, null, null);
        return cursor.getCount() > 0;
    }

    public String GetNickname(String my_user_id, String friend_user_id) {
        Cursor cursor = database.query("chatroom_user", null, "my_user_id = " + my_user_id + " and friend_user_id = " + friend_user_id, null, null, null, null,"1");
        if(cursor.getCount()>0) {
            cursor.moveToFirst();
            int a = cursor.getColumnIndex("nickname");
            String nickname = cursor.getString(a);
            cursor.close();
            return nickname;
        }
        cursor.close();
        return "";
    }

    public Cursor GetChatroomUser(int chatroom_id, String my_user_id){
        Cursor cursor = database.query("chatroom_user",null, "my_user_id = " + my_user_id + " and chatroom_id = "+ chatroom_id + " and join_type != 0", null, null, null,"CASE WHEN nickname GLOB '[a-zA-Z]*' THEN 2 WHEN nickname GLOB '[0-9]*' THEN 3 ELSE 1 END, nickname ASC");
        return cursor;
    }

    public void close() {
        db_helper.close();
    }
}
