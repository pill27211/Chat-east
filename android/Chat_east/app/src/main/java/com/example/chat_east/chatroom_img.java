package com.example.chat_east;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class chatroom_img extends AppCompatActivity {
    int chatroom_id;
    String my_user_id;
    chat_db_manager chat_db_manager;
    RecyclerView img_recycler_view;
    TextView chatroom_name;
    private static final int TYPE_DATE = 0;
    private static final int TYPE_PHOTO = 1;
    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        setContentView(R.layout.chatroom_img);
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("chatroom_key")) {
            chatroom_id = intent.getIntExtra("chatroom_key",1);
        }
        my_user_id = message_receive.user_id;
        chat_db_manager = new chat_db_manager(this);
        img_recycler_view = findViewById(R.id.img_recycler_view);
        chatroom_name = findViewById(R.id.chatroom_name);

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy. M. d");

        List<Object> item_list = new ArrayList<>();
        Map<String, List<Photo>> group_photomap = new LinkedHashMap<>();

        Cursor cursor = chat_db_manager.GetImg(chatroom_id);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String create_time = cursor.getString(cursor.getColumnIndexOrThrow("create_time"));
                String content = cursor.getString(cursor.getColumnIndexOrThrow("content"));
                int message_id = cursor.getInt(cursor.getColumnIndexOrThrow("message_id"));

                String date = null;
                try {
                    Date parse_date = dateTimeFormat.parse(create_time);
                    date = dateFormat.format(parse_date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Photo photo = new Photo(date, content, message_id);

                if (!group_photomap.containsKey(date)) {
                    group_photomap.put(date, new ArrayList<>());
                }

                group_photomap.get(date).add(photo);

            } while (cursor.moveToNext());
            cursor.close();
        }

        for (String date : group_photomap.keySet()) {
            item_list.add(date);
            item_list.addAll(group_photomap.get(date));
        }

        chatroom_img_adapter adapter = new chatroom_img_adapter(this,item_list);
        img_recycler_view.setAdapter(adapter);

        GridLayoutManager grid_manager = new GridLayoutManager(this, 3);

        grid_manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (adapter.getItemViewType(position) == TYPE_DATE) ? 3 : 1;
            }
        });

        adapter.photo_click  = new chatroom_img_adapter.PhotoClick() {
            @Override
            public void onItemClick(String img_url, int message_id) {
                // 클릭된 이미지와 message_id를 처리하는 로직 구현
                // 예: 이미지 확대, 다른 액티비티로 전환, 등등
                Intent intent = new Intent(chatroom_img.this, photo_view.class);
                intent.putExtra("img_url", img_url);

                intent.putExtra("message_id", message_id);
                startActivity(intent);
            }
        };
        img_recycler_view.setLayoutManager(grid_manager);
        String chatroom_names = chat_db_manager.GetChatroomName(chatroom_id);
        chatroom_name.setText(chatroom_names);
    }

    public void Back(View view){
        finish();
    }

    public class Photo {
        String date;
        String content;
        int message_id;

        public Photo(String date, String content, int message_id) {
            this.date = date;
            this.content = content;
            this.message_id = message_id;
        }
    }
}


