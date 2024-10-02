package com.example.chat_east;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class notification_check extends AppCompatActivity {

    int chatroom_id, message_type;
    String friend_user_id;

    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("friend_user_id")) {
            friend_user_id = intent.getStringExtra("friend_user_id");
        }
        if (intent != null && intent.hasExtra("message_type")) {
            message_type = intent.getIntExtra("message_type",0);
            if(message_type == 10 && message_receive.user_id!=null){//만약 전화이고 앱 켜져이ㅅ으면
                intent = new Intent(this, call.class);
                intent.putExtra("friend_user_id", friend_user_id);
                intent.putExtra("call_type", 1);
            }
            else if(message_type == 10 && message_receive.user_id==null){//만약 전화이고 앱 꺼져있으면
                intent = new Intent(this, login_check.class);
                intent.putExtra("friend_user_id", friend_user_id);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
            return;
        }

        if (intent != null && intent.hasExtra("chatroom_id")) {
            chatroom_id = intent.getIntExtra("chatroom_id",0);
        }

        if(message_receive.user_id!=null){
            intent = new Intent(this, main_chatting.class);
        }else{
            intent = new Intent(this, login_check.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("chatroom_id", chatroom_id);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Intent intent2 = getIntent();
        if (intent2 != null && intent2.hasExtra("chatroom_id")) {
            chatroom_id = intent2.getIntExtra("chatroom_id",0);
        }
        if(message_receive.user_id!=null){
            intent2 = new Intent(this, main_chatting.class);
        }else{
            intent2 = new Intent(this, login_check.class);
        }

        intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent2.putExtra("chatroom_id", chatroom_id);
        startActivity(intent2);
        finish();
    }

}
