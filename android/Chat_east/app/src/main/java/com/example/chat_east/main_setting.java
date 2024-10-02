package com.example.chat_east;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chat_east.setting.friends_activity;
import com.example.chat_east.setting.logout_activity;
import com.example.chat_east.setting.notifications_activity;
import com.example.chat_east.setting.profile_activity;

public class main_setting extends AppCompatActivity {

    public String my_user_id, my_nickname, my_email, my_user_tag;

    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        setContentView(R.layout.main_setting);

        my_user_id = message_receive.user_id;
        my_email = message_receive.email;
        my_nickname = message_receive.nickname;
        my_user_tag = message_receive.user_tag;

        TextView friend_option = findViewById(R.id.friends_option);
        TextView notifications_option = findViewById(R.id.notifications_option);
        TextView logout_option = findViewById(R.id.logout_option);
        TextView profile_phone = findViewById(R.id.profile_phone);
        TextView nickname_phone = findViewById(R.id.profile_name);

        profile_phone.setText(my_email);
        nickname_phone.setText(my_nickname);

        View manage_view = findViewById(R.id.manage_view);
        manage_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profile_intent = new Intent(main_setting.this, profile_activity.class);
                startActivity(profile_intent);
            }
        });

        friend_option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent friends_intent = new Intent(main_setting.this, friends_activity.class);
                startActivity(friends_intent);
            }
        });

        notifications_option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent notifications_intent = new Intent(main_setting.this, notifications_activity.class);
                startActivity(notifications_intent);
            }
        });

        logout_option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent logout_intent = new Intent(main_setting.this, logout_activity.class);
                startActivity(logout_intent);
            }
        });
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

}
