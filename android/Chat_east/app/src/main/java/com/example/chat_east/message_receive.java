package com.example.chat_east;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class message_receive extends Service {
    private chat_db_manager db_manager;

    public static final String user_id_key = "1";
    public static final String nickname_key = "";
    public static final String email_key = "email";
    public static final String user_tag_key = "email";

    public static String user_id=null;
    public static String nickname;
    public static String email;
    public static String user_tag;

    public static boolean inchatroom = false;

    @Override
    public void onCreate() {
        super.onCreate();
        db_manager = new chat_db_manager(this);
        SendEmailBroadcast();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (intent.hasExtra(user_id_key)) {
                user_id = intent.getStringExtra(user_id_key);
            }
            if (intent.hasExtra(nickname_key)) {
                nickname = intent.getStringExtra(nickname_key);
            }
            if (intent.hasExtra(email_key)) {
                email = intent.getStringExtra(email_key);
            }
            if (intent.hasExtra(user_tag_key)) {
                user_tag = intent.getStringExtra(user_tag_key);
            }
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    void SendEmailBroadcast() {
        if (email != null && !email.isEmpty()) {
            Intent intent = new Intent("com.example.chat_east.email_event");
            intent.putExtra("email", email);
            sendBroadcast(intent);
        }
    }

    private void RunOnUiThread(Runnable runnable) {
        new android.os.Handler(getMainLooper()).post(runnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("message recieve","die");
        stopSelf();
    }
}
