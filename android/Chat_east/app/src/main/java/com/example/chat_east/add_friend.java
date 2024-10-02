package com.example.chat_east;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class add_friend extends AppCompatActivity {
    private Socket msocket;
    private friend_db_manager friend_db_manager;
    private chat_db_manager chat_db_manager;


    public static final String user_id_key = "1";
    public static final String nickname_key = "";
    public String my_user_id;
    public String my_nickname;
    public String friend_user_tag;
    private String friend_data;
    private String friend_user_id;
    String friend_nickname;
    String friend_comment;
    String friend_profile_img_url;
    String friend_background_img_url;
    String friend_last_profile_update;
    View profile_image;
    TextView user_name;
    TextView cant_find_tag;
    TextView ignore_button;
    TextView add_button;
    TextView chatting_button;
    TextView ignore_cancel_button;
    EditText user_tag_input;

    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        setContentView(R.layout.add_friend);

        profile_image = findViewById(R.id.profile_image);
        cant_find_tag = findViewById(R.id.cant_find_tag);
        user_name = findViewById(R.id.user_name);
        ignore_button = findViewById(R.id.ignore_button);
        add_button = findViewById(R.id.add_button);
        chatting_button = findViewById(R.id.chatting_button);
        ignore_cancel_button = findViewById(R.id.ignore_cancel_button);
        user_tag_input = findViewById(R.id.user_tag_input);

        my_user_id = message_receive.user_id;
        my_nickname = message_receive.nickname;

        friend_db_manager = new friend_db_manager(this);
        chat_db_manager = new chat_db_manager(this);
        Connect();

        user_tag_input.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent key_event) {
                if(i == KeyEvent.KEYCODE_ENTER && key_event.getAction() == KeyEvent.ACTION_DOWN) {
                    SearchTag(null);
                    return true;
                }
                return false;
            }
        });
    }

    void Connect() {
        try {
            //msocket = IO.socket("http://10.0.2.2:3000/");//안드로이드 avd사용시 로컬 호스트는 이 주소사용
            msocket = IO.socket("http://34.22.99.247:3000/");
            msocket.connect();
            msocket.on("search_tag", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject data = (JSONObject) args[0];
                                friend_data = data.getString("friend_data");
                                Log.d("search_tag",friend_data);
                                if(Objects.equals(friend_data, "null")){
                                    profile_image.setVisibility(View.INVISIBLE);
                                    cant_find_tag.setVisibility(View.VISIBLE);
                                    user_name.setVisibility(View.INVISIBLE);
                                    ignore_button.setVisibility(View.INVISIBLE);
                                    add_button.setVisibility(View.INVISIBLE);
                                    chatting_button.setVisibility(View.INVISIBLE);
                                    ignore_cancel_button.setVisibility(View.INVISIBLE);
                                }
                                else if(!Objects.equals(friend_data, "[]")){
                                    friend_data = ((JSONObject) args[0]).toString();
                                    SearchFriendData(friend_data);
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
            msocket.on("create_chatroom", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject data = (JSONObject) args[0];
                                String chatroom_id = data.getString("chatroom_id");
                                String chatroom_name = data.getString("chatroom_name");

                                chat_db_manager.AddChatroomList(my_user_id, Integer.parseInt(chatroom_id), chatroom_name, 2, 1, 1, "", "2020-01-01 00:00:00", 2, "", "2020-01-01 00:00:00", "2020-01-01 00:00:00");

                                Intent intent = new Intent(add_friend.this, main_chatting.class);
                                intent.putExtra(chatting_room.user_id_key, my_user_id);
                                intent.putExtra(chatting_room.nickname_key, my_nickname);
                                intent.putExtra("chatroom_id", Integer.parseInt(chatroom_id));

                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                startActivity(intent);
                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                            } catch (JSONException e) {
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

    void SearchFriendData(String friend_data) {
        try {
            JSONObject jsonobject = new JSONObject(friend_data);
            JSONArray friend_array = jsonobject.getJSONArray("friend_data");
            for (int i = 0; i < friend_array.length(); i++) {
                JSONObject friend_array_json = friend_array.getJSONObject(i);
                friend_user_id = friend_array_json.getString("user_id");
                friend_nickname = friend_array_json.getString("nickname");
                friend_comment = friend_array_json.getString("comment");
                friend_profile_img_url = friend_array_json.getString("profile_img_url");
                friend_background_img_url = friend_array_json.getString("background_img_url");
                friend_last_profile_update = friend_array_json.getString("last_profile_update");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(Objects.equals(my_user_id, friend_user_id)){
            add_button.setVisibility(View.INVISIBLE);
            ignore_button.setVisibility(View.INVISIBLE);
            chatting_button.setVisibility(View.VISIBLE);
            ignore_cancel_button.setVisibility(View.INVISIBLE);
        }
        else if(friend_db_manager.IsFriend(my_user_id, friend_user_id)){//친구가 이미 되어있으면
            add_button.setVisibility(View.INVISIBLE);
            ignore_button.setVisibility(View.INVISIBLE);
            chatting_button.setVisibility(View.VISIBLE);
            ignore_cancel_button.setVisibility(View.INVISIBLE);
        }else if(friend_db_manager.FindIgnoreUserID(my_user_id, friend_user_id)){//차단된 유저라면
            add_button.setVisibility(View.INVISIBLE);
            ignore_button.setVisibility(View.INVISIBLE);
            chatting_button.setVisibility(View.INVISIBLE);
            ignore_cancel_button.setVisibility(View.VISIBLE);
        }else{
            ignore_button.setVisibility(View.VISIBLE);
            add_button.setVisibility(View.VISIBLE);
            chatting_button.setVisibility(View.INVISIBLE);
            ignore_cancel_button.setVisibility(View.INVISIBLE);
        }
        profile_image.setVisibility(View.VISIBLE);
        user_name.setVisibility(View.VISIBLE);
        user_name.setText(friend_nickname);
        cant_find_tag.setVisibility(View.INVISIBLE);
    }

    public void BackActivity(View view){
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
    public void SearchTag(View view){
        Log.d("search_tag","search_tag clicked");
        friend_user_tag = user_tag_input.getText().toString();
        if (msocket != null) {
            JSONObject data = new JSONObject();
            try {
                data.put("user_tag",friend_user_tag);
                msocket.emit("search_tag", data);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }
    public void AddFriend(View view){
        Log.d("add_friend","add_friend clicked");
        if (msocket != null) {
            JSONObject data = new JSONObject();
            try {
                data.put("from_user_id", my_user_id);
                data.put("to_user_id",friend_user_id);
                msocket.emit("add_friend", data);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
        friend_db_manager.FriendUpdate(my_user_id, friend_user_id, friend_nickname,friend_comment, 1, friend_profile_img_url, friend_background_img_url, friend_last_profile_update);
        add_button.setVisibility(View.INVISIBLE);
        ignore_button.setVisibility(View.INVISIBLE);
        chatting_button.setVisibility(View.VISIBLE);
    }
    public void IgnoreFriend(View view){
        Log.d("ignore_friend","ignore_friend clicked");
        if (msocket != null) {
            JSONObject data = new JSONObject();
            try {
                data.put("from_user_id", my_user_id);
                data.put("to_user_id",friend_user_id);
                msocket.emit("ignore_friend", data);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
        friend_db_manager.FriendUpdate(my_user_id, friend_user_id, friend_nickname, friend_comment, 3, friend_profile_img_url, friend_background_img_url, friend_last_profile_update);
        add_button.setVisibility(View.INVISIBLE);
        ignore_button.setVisibility(View.INVISIBLE);
        chatting_button.setVisibility(View.INVISIBLE);
        ignore_cancel_button.setVisibility(View.VISIBLE);
    }
    public void IgnoreCancel(View view){
        Log.d("ignore_cancel","ignore_cancel clicked");
        if (msocket != null) {
            JSONObject data = new JSONObject();
            try {
                data.put("from_user_id", my_user_id);
                data.put("to_user_id",friend_user_id);
                msocket.emit("ignore_cancel", data);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
        friend_db_manager.IgnoreDelete(my_user_id, friend_user_id);
        add_button.setVisibility(View.VISIBLE);
        ignore_button.setVisibility(View.VISIBLE);
        chatting_button.setVisibility(View.INVISIBLE);
        ignore_cancel_button.setVisibility(View.INVISIBLE);
    }

    public void FriendChatting(View view){
        if (msocket != null) {
            JSONObject data = new JSONObject();

            try {
                data.put("user_id", my_user_id);
                data.put("people_count", 2);
                data.put("chatroom_name", friend_nickname);
                data.put("my_nickname", friend_db_manager.GetMyNickname(my_user_id));
                JSONArray user_id_list = new JSONArray();
                user_id_list.put(my_user_id);

                user_id_list.put(friend_user_id);
                data.put("friend_nickname", friend_nickname);
                Log.d("friend_nickname",friend_nickname);
                data.put("user_id_list", user_id_list);

                int chatroom_type = 2;
                msocket.emit("create_chatroom", data);
                msocket.on("create_chatroom", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    JSONObject data = (JSONObject) args[0];
                                    String chatroom_id = data.getString("chatroom_id");
                                    String chatroom_name = data.getString("chatroom_name");
                                    Log.d("chatroom_type",String.valueOf(chatroom_type));

                                    chat_db_manager.AddChatroomList(my_user_id, Integer.parseInt(chatroom_id), chatroom_name, chatroom_type, 1, 1,"", "2020-01-01 00:00:00", 2, "", "2020-01-01 00:00:00", "2020-01-01 00:00:00");

                                    Intent intent = new Intent(add_friend.this, main_chatting.class);
                                    intent.putExtra(chatting_room.user_id_key, my_user_id);
                                    intent.putExtra(chatting_room.nickname_key, my_nickname);
                                    intent.putExtra("chatroom_id", Integer.parseInt(chatroom_id));

                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                    startActivity(intent);
                                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
        }
    }
    @Override
    protected void onStop() {//액티비티 종료시 실행
        super.onStop();
        if (msocket != null) {
            msocket.disconnect();//소켓을 닫는다
        }
        friend_db_manager.close();
    }
    @Override
    protected void onDestroy() {//어플리케이션 종료시 실행
        super.onDestroy();
        if (msocket != null) {
            msocket.disconnect(); // 소켓을 닫는다
        }
        friend_db_manager.close();
    }
}