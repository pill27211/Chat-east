package com.example.chat_east;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Objects;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class main_friend extends AppCompatActivity {
    private Socket msocket;
    private friend_db_manager db_manager;

    public String my_user_id, friend_user_id="", friend_data, my_email, my_user_tag;
    public static String my_nickname;

    private RecyclerView recycler_view;
    private friend_list_adapter friend_list_adapter;
    private profile_adapter profile_adapter;

    Cursor cursor, my_profile_cursor, friend_list_cursor, new_my_profile_cursor, new_friend_cursor;

    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        setContentView(R.layout.main_friend);

        my_user_id = message_receive.user_id;
        my_email = message_receive.email;
        my_nickname = message_receive.nickname;
        my_user_tag = message_receive.user_tag;

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("friend_user_id")) {
            friend_user_id = intent.getStringExtra("friend_user_id");
            if(!Objects.equals(friend_user_id, null)) {
                Intent call_intent = new Intent(getApplicationContext(), call.class);
                call_intent.putExtra("friend_user_id", friend_user_id);
                call_intent.putExtra("call_type", 1);
                startActivity(call_intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        }
        db_manager = new friend_db_manager(this);
        Connect();
        CheckUpdate();
        FriendListShow();

    }

    void Connect() {
        try {
            //msocket = IO.socket("http://10.0.2.2:3000/");//안드로이드 avd사용시 로컬 호스트는 이 주소사용
            msocket = IO.socket("http://34.22.99.247:3000/");
            msocket.connect();
            msocket.on("friend_update", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject data = (JSONObject) args[0];
                                friend_data = data.getString("friend_data");
                                Log.d("Friend_check_update",friend_data);
                                if(!Objects.equals(friend_data, "null")){
                                    friend_data = ((JSONObject) args[0]).toString();
                                    UpdateFriend(friend_data);
                                }
                                FriendListShow();
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
                cursor = db_manager.GetLastTime(my_user_id);
                if (cursor.moveToFirst()) {
                    String friend_last_update_time = cursor.getString(cursor.getColumnIndexOrThrow("update_time"));
                    data.put("update_time",friend_last_update_time);
                    Log.d("update_time",friend_last_update_time);
                }else{
                    data.put("update_time","2020-01-01 00:00:00");
                }
                msocket.emit("check_friend_update", data);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    void UpdateFriend(String friend_data) {
        try {
            JSONObject jsonobject = new JSONObject(friend_data);
            JSONArray friend_array = jsonobject.getJSONArray("friend_data");
            for (int i = 0; i < friend_array.length(); i++) {
                JSONObject friend_array_json = friend_array.getJSONObject(i);
                String friend_user_id = friend_array_json.getString("user_id");
                String nickname = friend_array_json.getString("nickname");
                String comment = friend_array_json.getString("comment");
                int friend_type = friend_array_json.getInt("friend_type");
                String profile_img_url = friend_array_json.getString("profile_img_url");
                String background_img_url = friend_array_json.getString("background_img_url");
                String last_profile_update = friend_array_json.getString("last_profile_update");
                db_manager.FriendUpdate(my_user_id, friend_user_id, nickname, comment, friend_type, profile_img_url, background_img_url, last_profile_update);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void FriendListShow(){
        recycler_view = findViewById(R.id.friend_list);
        recycler_view.setLayoutManager(new LinearLayoutManager(this));

        my_profile_cursor = db_manager.ShowProfile(my_user_id);
        friend_list_cursor = db_manager.ShowFriend(my_user_id);

        profile_adapter = new profile_adapter(this,my_profile_cursor);
        friend_list_adapter = new friend_list_adapter(this,friend_list_cursor, new friend_list_adapter.OnItemClickListener(){
            @Override
            public void onItemClick(String friend_user_id) {
                FriendProfileClick(friend_user_id);
            }
        });

        ConcatAdapter concatadapter = new ConcatAdapter(profile_adapter, friend_list_adapter);
        recycler_view.setAdapter(concatadapter);
    }

    public void MyProfileClick(View view){
        Intent profile = new Intent(this, profile_view.class);
        profile.putExtra(profile_view.view_user_id_key, my_user_id);
        startActivity(profile);
    }

    public void FriendProfileClick(String friend_user_id){
        Intent profile = new Intent(this, profile_view.class);
        profile.putExtra(profile_view.view_user_id_key, friend_user_id);
        startActivity(profile);
    }
    public void AddFriendClick(View view) {
        Intent add_friends = new Intent(this, add_friend.class);
        startActivity(add_friends);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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
        finish();
    }
    public void BottomBarSettingClick(View view) {
        Intent setting_intent = new Intent(this, main_setting.class);
        startActivity(setting_intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
    @Override
    protected void onStop() {//액티비티 종료시 실행
        super.onStop();
        if (msocket != null) {
            msocket.disconnect();//소켓을 닫는다
        }
        db_manager.close();
    }
    @Override
    protected void onDestroy() {//어플리케이션 종료시 실행
        super.onDestroy();
        if (msocket != null) {
            msocket.disconnect(); // 소켓을 닫는다
        }
        db_manager.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        db_manager = new friend_db_manager(this);
        if(message_receive.user_id==null){
            Log.d("error","error");
            Intent call_intent = new Intent(getApplicationContext(), login_check.class);
            call_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(call_intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
        new_my_profile_cursor = db_manager.ShowProfile(my_user_id);
        new_friend_cursor = db_manager.ShowFriend(my_user_id);
        profile_adapter = new profile_adapter(this,new_my_profile_cursor);
        friend_list_adapter = new friend_list_adapter(this, new_friend_cursor, new friend_list_adapter.OnItemClickListener(){
            @Override
            public void onItemClick(String friend_user_id) {
                FriendProfileClick(friend_user_id);
            }
        });
        ConcatAdapter concatadapter = new ConcatAdapter(profile_adapter, friend_list_adapter);
        recycler_view.setAdapter(concatadapter);
    }
}
