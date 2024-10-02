package com.example.chat_east;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Objects;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class main_chatting extends AppCompatActivity {
    private chat_db_manager db_manager;
    private Socket msocket;

    public static final String user_id_key = "1";
    public static final String nickname_key = "";
    public static final String email_key = "email";
    public static final String user_tag_key = "tag";
    public String my_user_id, my_nickname, last_view_time, my_email, my_user_tag, chatroom_list_data;
    public int chatroom_id;

    private RecyclerView recycler_view;
    private chatroom_list_adapter chatroom_list_adapter;
    Cursor chatroom_cursor;

    private boolean receive_register = false;

    private BroadcastReceiver socket_receiver = new BroadcastReceiver() {
        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.example.chat_east.socket_event".equals(intent.getAction())) {
                String socket_event = intent.getStringExtra("socket_event");
                CheckUpdate();
                ChatroomListShow();
                Log.d("hi","hi");
                chatroom_list_adapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        setContentView(R.layout.main_chatting);
        my_user_id = message_receive.user_id;
        my_email = message_receive.email;
        my_nickname = message_receive.nickname;
        my_user_tag = message_receive.user_tag;

        IntentFilter filter = new IntentFilter("com.example.chat_east.socket_event");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(socket_receiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(socket_receiver, filter);
        }
        receive_register = true;
        db_manager = new chat_db_manager(this);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("chatroom_id")) {
            chatroom_id = intent.getIntExtra("chatroom_id",0);
            Log.d("chatroom_id",String.valueOf(chatroom_id));
            if(chatroom_id!=0){
                MoveToChatroom(chatroom_id);
            }
        }

        ChatroomListShow();
    }

    void Connect() {
        Log.d("SOCKET","Connect 함수 실행");
        try {
            //msocket = IO.socket("http://10.0.2.2:3000/");//안드로이드 avd사용시 로컬 호스트는 이 주소사용
            msocket = IO.socket("http://34.22.99.247:3000/");
            msocket.connect();
            msocket.on("chatroom_list_update", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject data = (JSONObject) args[0];
                                chatroom_list_data = data.getString("chatroom_list_data");
                                Log.d("chatroom_list_update",chatroom_list_data);
                                if(!Objects.equals(chatroom_list_data, "null")){
                                    chatroom_list_data = ((JSONObject) args[0]).toString();
                                    UpdateChatroomList(chatroom_list_data);
                                }
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
        } catch (Exception e) {
            Log.e("SOCKET", "Connection error", e);
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    void CheckUpdate() {//업데이트된 친구의 프로필이 있는지 확인
        if (msocket != null) {
            JSONObject data = new JSONObject();
            try {
                data.put("user_id", my_user_id);
                data.put("update_time",db_manager.GetListLastTime(my_user_id));
                msocket.emit("chatroom_list_update", data);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    void UpdateChatroomList(String friend_data) {
        try {
            JSONObject jsonobject = new JSONObject(friend_data);
            JSONArray chatroom_list_array = jsonobject.getJSONArray("chatroom_list_data");
            for (int i = 0; i < chatroom_list_array.length(); i++) {
                JSONObject chatroom_list_json = chatroom_list_array.getJSONObject(i);
                int chatroom_id = chatroom_list_json.getInt("chatroom_id");
                String chatroom_name = chatroom_list_json.getString("chatroom_name");
                int chatroom_type = chatroom_list_json.getInt("chatroom_type");
                int favorite_type = chatroom_list_json.getInt("favorite_type");
                int notification_type = chatroom_list_json.getInt("notification_type");
                String chatroom_img_url = chatroom_list_json.getString("chatroom_img_url");
                String update_time = chatroom_list_json.getString("update_time");
                int people_count = chatroom_list_json.getInt("people_count");
                String last_message = chatroom_list_json.getString("last_message");
                String last_message_time = chatroom_list_json.getString("last_message_time");
                String last_view_time = chatroom_list_json.getString("last_view_time");
                db_manager.AddChatroomList(my_user_id, chatroom_id, chatroom_name, chatroom_type, favorite_type, notification_type, chatroom_img_url, update_time, people_count, last_message, last_message_time, last_view_time);
                ChatroomListShow();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void CreateChatroom(View view){
        Intent chatroom_invite = new Intent(this, chatroom_invite.class);
        chatroom_invite.putExtra("my_user_id", my_user_id);
        chatroom_invite.putExtra("my_nickname", my_nickname);
        startActivity(chatroom_invite);
    }

    void ChatroomListShow(){
        recycler_view = findViewById(R.id.chatting_room_list);
        recycler_view.setLayoutManager(new LinearLayoutManager(this));
        chatroom_cursor = db_manager.ShowChatroomList(my_user_id);
        chatroom_list_adapter = new chatroom_list_adapter(this, my_user_id, chatroom_cursor, new chatroom_list_adapter.OnItemClickListener(){
            @Override
            public void onItemClick(int chatroom_id) {
                MoveToChatroom(chatroom_id);
            }
        });
        recycler_view.setAdapter(chatroom_list_adapter);
    }
    
    public void BottomBarFriendClick(View view) {
        Intent friend_intent = new Intent(this, main_friend.class);
        startActivity(friend_intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
    public void BottomBarChattingClick(View view) {
        Intent chatting_intent = new Intent(this, main_chatting.class);
        startActivity(chatting_intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
    public void BottomBarSettingClick(View view) {
        Intent setting_intent = new Intent(this, main_setting.class);
        startActivity(setting_intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
    public void MoveToChatroom(int chatroom_id) {
        Intent chattingroom = new Intent(this, chatting_room.class);
        chattingroom.putExtra("chatroom_key", chatroom_id);
        startActivity(chattingroom);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    protected void onStop() {//액티비티 종료시 실행
        super.onStop();
        if (msocket != null) {
            msocket.disconnect();//소켓을 닫는다
        }
        if(receive_register) {
            unregisterReceiver(socket_receiver);
            receive_register = false;
        }
        if(chatroom_list_adapter!=null) {
            chatroom_list_adapter.close();
        }
    }

    @Override
    protected void onDestroy() {//어플리케이션 종료시 실행
        super.onDestroy();
        if (msocket != null) {
            msocket.disconnect(); // 소켓을 닫는다
        }
        if(receive_register) {
            unregisterReceiver(socket_receiver);
            receive_register = false;
        }
        if(chatroom_list_adapter!=null) {
            chatroom_list_adapter.close();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(receive_register){
            unregisterReceiver(socket_receiver);
            receive_register = false;
        }
        IntentFilter filter = new IntentFilter("com.example.chat_east.socket_event");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(socket_receiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(socket_receiver, filter);
        }
        receive_register = true;
        db_manager = new chat_db_manager(this);

        if(msocket==null){
            Connect();
        }else if(!msocket.connected()){
            Connect();
        }
        CheckUpdate();
        ChatroomListShow();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra("chatroom_id")) {
            chatroom_id = intent.getIntExtra("chatroom_id",0);
            Log.d("chatroom_id",String.valueOf(chatroom_id));
            if(chatroom_id!=0){
                MoveToChatroom(chatroom_id);
            }
        }
    }
}
