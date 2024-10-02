package com.example.chat_east.setting;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Switch;
import android.widget.TextView;
import com.example.chat_east.R;
import com.example.chat_east.message_receive;

public class profile_activity extends AppCompatActivity {

    public static final String user_id_key = "1";
    public static final String nickname_key = "";
    public static final String email_key = "email";
    public static final String user_tag_key = "tag";
    public String my_user_id;
    public String my_nickname;
    public String my_email;
    public String my_user_tag;

    private TextView email_value, nickname_value, chat_east_id;
    private Switch birthday_notification_switch;

    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        setContentView(R.layout.activity_profile);

        email_value = findViewById(R.id.email_value);
        chat_east_id = findViewById(R.id.kakaotalk_id_value);
        nickname_value = findViewById(R.id.nickname_value);
        birthday_notification_switch = findViewById(R.id.birthday_notification_switch);

        my_user_id = message_receive.user_id;
        my_email = message_receive.email;
        my_nickname = message_receive.nickname;
        my_user_tag = message_receive.user_tag;


        email_value.setText(my_email);
        chat_east_id.setText(my_user_tag);
        nickname_value.setText(my_nickname);

        birthday_notification_switch.setOnCheckedChangeListener((buttonView, is_checked) -> {
            if (is_checked) {
            }
            else {
            }
        });
    }
}
