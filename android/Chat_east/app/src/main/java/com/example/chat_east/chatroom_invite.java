package com.example.chat_east;

import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class chatroom_invite extends AppCompatActivity {
    private Socket msocket;
    private String my_user_id;
    private String my_nickname;
    private int chatroom_id;
    private RecyclerView friend_list;
    private TextView confirm_text;
    private TextView confirm_text2;
    private friend_db_manager friend_db_manager;
    private chat_db_manager chat_db_manager;
    private invite_friend_list_adapter invite_friend_list_adapter;
    private invite_friend_search_adapter invite_friend_search_adapter;
    private Map<String, Boolean> user_id_map = new HashMap<>();
    private int friend_count=0;

    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        setContentView(R.layout.invite_chatroom);

        friend_db_manager = new friend_db_manager(this);
        chat_db_manager = new chat_db_manager(this);

        friend_list = findViewById(R.id.friend_list);
        confirm_text = findViewById(R.id.confirm_text);
        confirm_text2 = findViewById(R.id.confirm_text2);

        my_user_id = message_receive.user_id;
        my_nickname = message_receive.nickname;

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("chatroom_id")) {
            chatroom_id = intent.getIntExtra("chatroom_id", 0);
        }

        if(chatroom_id==0){
            TextView invite_text = findViewById(R.id.invite_text);
            invite_text.setText("멤버 초대");
            confirm_text.setText("다음");
            confirm_text2.setText("다음");
        }

        SetFriendList();
        if(chatroom_id!=0) {
            confirm_text2.setOnClickListener(v -> {
                Set<String> selectedFriends = invite_friend_list_adapter.getSelectedFriends();
                SendInvite(selectedFriends);
            });
        }
        else{
            confirm_text2.setOnClickListener(v -> {
                Set<String> selectedFriends = invite_friend_list_adapter.getSelectedFriends();
                MadeChatroom(selectedFriends);
            });
        }
    }

    void FriendList(String number){
        if (user_id_map.containsKey(number)) {
            user_id_map.remove(number);
            friend_count--;
            System.out.println("Removed number: " + number);
        } else {
            user_id_map.put(number, true);
            friend_count++;
            System.out.println("Added number: " + number);
        }

        if(friend_count==0){
            confirm_text.setVisibility(View.VISIBLE);
            confirm_text2.setVisibility(View.GONE);
        }else{
            confirm_text.setVisibility(View.GONE);
            confirm_text2.setVisibility(View.VISIBLE);
        }
    }

    void SetFriendList() {
        friend_list.setLayoutManager(new LinearLayoutManager(this));
        Cursor friend_list_cursor = friend_db_manager.ShowFriend(my_user_id);
        if(chatroom_id==0) {
            invite_friend_list_adapter = new invite_friend_list_adapter(this, friend_list_cursor, my_user_id, chatroom_id, friend_user_id -> {
                FriendList(friend_user_id);
            });
            friend_list.setAdapter(invite_friend_list_adapter);
        }else{
            Cursor chatroom_cursor = chat_db_manager.ChatroomCursor(my_user_id, chatroom_id);
            Cursor new_cursor = CombineCursor(friend_list_cursor, chatroom_cursor);

            invite_friend_search_adapter = new invite_friend_search_adapter();
            invite_friend_list_adapter = new invite_friend_list_adapter(this, new_cursor, my_user_id, chatroom_id, friend_user_id -> {
                FriendList(friend_user_id);
            });
            ConcatAdapter concatAdapter = new ConcatAdapter(invite_friend_search_adapter, invite_friend_list_adapter);
            friend_list.setAdapter(concatAdapter);
        }
    }

    public Cursor CombineCursor(Cursor friend_list_cursor, Cursor chatroom_cursor) {
        Set<String> friendUserIdsInChatroom = new HashSet<>();

        if (chatroom_cursor != null) {
            while (chatroom_cursor.moveToNext()) {
                String friend_user_id = chatroom_cursor.getString(chatroom_cursor.getColumnIndexOrThrow("friend_user_id"));
                friendUserIdsInChatroom.add(friend_user_id);
            }
            chatroom_cursor.close();
        }

        MatrixCursor new_cursor = new MatrixCursor(friend_list_cursor.getColumnNames());

        if (friend_list_cursor != null) {
            while (friend_list_cursor.moveToNext()) {
                String friend_user_id = friend_list_cursor.getString(friend_list_cursor.getColumnIndexOrThrow("friend_user_id"));
                if (!friendUserIdsInChatroom.contains(friend_user_id)) {
                    MatrixCursor.RowBuilder rowBuilder = new_cursor.newRow();
                    for (int i = 0; i < friend_list_cursor.getColumnCount(); i++) {
                        rowBuilder.add(friend_list_cursor.getString(i));
                    }
                }
            }
            friend_list_cursor.close();
        }
        return new_cursor;
    }

    void MadeChatroom(Set<String> select_friends) {
        Intent intent = new Intent(chatroom_invite.this, create_chatroom.class);
        intent.putExtra("select_friends", new ArrayList<>(select_friends));
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    void SendInvite(Set<String> select_friends) {
        try {
            msocket = IO.socket("http://34.22.99.247:3000/");
            msocket.connect();
            JSONObject data = new JSONObject();
            data.put("user_id", my_user_id);
            Log.d("chatroom_id", String.valueOf(chatroom_id));
            data.put("chatroom_id", chatroom_id);

            JSONArray user_id_array = new JSONArray();
            JSONArray user_nickname_array = new JSONArray();
            for (String friend_user_id : select_friends) {
                user_id_array.put(friend_user_id);
                user_nickname_array.put(friend_db_manager.GetNickname(my_user_id,friend_user_id));
            }
            data.put("friend_list", user_id_array);
            data.put("user_nickname_list",user_nickname_array);
            msocket.emit("invite_user", data);
            msocket.on("invite_user", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject data = (JSONObject) args[0];
                                int new_chatroom_id = data.getInt("chatroom_id");
                                int people_count = data.getInt("people_count");
                                if (new_chatroom_id == 0) {
                                    msocket.disconnect();
                                    finish();
                                } else {
                                    chat_db_manager.AddChatroomList(my_user_id, new_chatroom_id, "그룹채팅", 4, 1, 1, "", "2020-01-01 00:00:00", people_count, "", "2020-01-01 00:00:00", "2020-01-01 00:00:00");
                                    Intent intent = new Intent(chatroom_invite.this, main_chatting.class);
                                    intent.putExtra("chatroom_id", new_chatroom_id);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    msocket.disconnect();
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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
            e.printStackTrace();
        }
    }
}


