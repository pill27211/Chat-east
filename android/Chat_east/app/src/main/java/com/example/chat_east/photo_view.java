package com.example.chat_east;

import static com.bumptech.glide.request.target.Target.SIZE_ORIGINAL;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.chrisbanes.photoview.PhotoView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class photo_view  extends AppCompatActivity {
    String img_url, my_user_id;
    int message_id;
    PhotoView photo_view;
    LinearLayout photo_topbar, photo_bottombar;
    TextView sender_nickname, send_time;
    boolean open_dialog = false, download_img = false;
    chat_db_manager chat_db_manager;
    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        setContentView(R.layout.photo_view);
        chat_db_manager = new chat_db_manager(this);
        photo_view = findViewById(R.id.photo_view);
        photo_view.setMaximumScale(5.0f);

        my_user_id = message_receive.user_id;

        photo_topbar = findViewById(R.id.photo_topbar);
        photo_bottombar = findViewById(R.id.photo_bottombar);
        sender_nickname = findViewById(R.id.sender_nickname);
        send_time = findViewById(R.id.send_time);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("img_url")) {
            img_url = intent.getStringExtra("img_url");
        }
        if (intent != null && intent.hasExtra("message_id")) {
            message_id = intent.getIntExtra("message_id",0);
        }

        Log.d("img_url",img_url);

        Glide.with(photo_view.this)
                .load(img_url)
                .override(SIZE_ORIGINAL)
                .diskCacheStrategy(DiskCacheStrategy.ALL) // 캐싱 전략 설정
                .into(photo_view);

        if(message_id!=0) {//message_id 0 = 다운 불가 [프로필 이미지, 프로필 배경 이미지]
            photo_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (open_dialog) {
                        photo_topbar.setVisibility(View.GONE);
                        photo_bottombar.setVisibility(View.GONE);
                        open_dialog = false;
                    } else {
                        photo_topbar.setVisibility(View.VISIBLE);
                        photo_bottombar.setVisibility(View.VISIBLE);
                        open_dialog = true;
                    }
                }
            });
            Cursor cursor = chat_db_manager.GetMessage(message_id);
            if(cursor.moveToFirst()){
                String friend_user_id = cursor.getString(cursor.getColumnIndexOrThrow("user_id"));
                String time = cursor.getString(cursor.getColumnIndexOrThrow("create_time"));
                if(Objects.equals(my_user_id, friend_user_id))sender_nickname.setText(message_receive.nickname);
                else sender_nickname.setText(chat_db_manager.GetNickname(my_user_id, friend_user_id));

                SimpleDateFormat original_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat new_format = new SimpleDateFormat("yyyy. M. d. a h:mm", Locale.getDefault());
                Date date = null;
                try {
                    date = original_format.parse(time);
                } catch (ParseException e) {    }
                String format_time = new_format.format(date);
                send_time.setText(format_time);
            }
        }
    }

    public void Back(View view){
        finish();
    }

    public void DownloadImg(View view){
        Log.d("img_url",img_url);
        if(!download_img) {
            download_img=true;
            new save_image_task_download(this, img_url, new save_image_task_download.SaveImageCallback() {
                @Override
                public void Onimagesaved(String image_url, String image_path) {
                    download_img=false;
                }
            }).execute(img_url, "ChatEast_" + ExtractFileNameFromURL(img_url) + ".jpg");
        }
    }

    private String ExtractFileNameFromURL(String url) {
        String[] parts = url.split("/images/");
        if (parts.length > 1) {
            String filepart = parts[1];
            int dotindex = filepart.indexOf('.');
            if (dotindex != -1) {
                return filepart.substring(0, dotindex);
            }
        }
        return "";
    }

}
