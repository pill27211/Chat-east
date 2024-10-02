package com.example.chat_east;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;

public class firebase_message_service extends FirebaseMessagingService {

    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "chat_notifications";
    private static final int NOTIFICATION_ID = 1;
    private chat_db_manager chat_db_manager;
    private int chatroom_id, message_type;
    private String voice_user_id;

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "새로운 FCM 토큰: " + token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remote_message) {
        super.onMessageReceived(remote_message);

        chat_db_manager = new chat_db_manager(this);
        chatroom_id = Integer.parseInt(remote_message.getData().get("chatroom_id"));
        int chatroom_type = Integer.parseInt(remote_message.getData().get("chatroom_type"));
        message_type = Integer.parseInt(remote_message.getData().get("msg_type"));
        voice_user_id = remote_message.getData().get("user_id");
        if(!chat_db_manager.IsChatroomExist(message_receive.user_id,chatroom_id)){
            chat_db_manager.AddChatroomList(message_receive.user_id, chatroom_id, remote_message.getData().get("title"), chatroom_type, 1, 1,"", "2020-01-01 00:00:00", 2, "", "2020-01-01 00:00:00", "2020-01-01 00:00:00");
        }

        Log.d(TAG, "발신자: " + remote_message.getFrom());
        if(message_type!=10) {
            int temp_msg_id = chat_db_manager.GetSmallMessageID(chatroom_id)-1;
            if(temp_msg_id>-1)temp_msg_id=-1;
            chat_db_manager.AddMessage("0", temp_msg_id, chatroom_id, remote_message.getData().get("body"), "", -1, 0, 0, 0, remote_message.getData().get("update_time"), remote_message.getData().get("update_time"));
            Log.d("asdasd","asdasd");
        }

        Intent intent = new Intent("com.example.chat_east.socket_event");
        intent.putExtra("socket_event", "msg_to_client");
        intent.setPackage(getPackageName());
        sendBroadcast(intent);

        if (remote_message.getNotification() != null) {
            Log.d(TAG, "알림 메시지 본문: " + remote_message.getNotification().getBody());
        }

        if(Objects.equals(remote_message.getData().get("notification_type"), "0")) {
        }else if(!message_receive.inchatroom){//채팅방에 없으면 무조건 알림보냄
            SendNotification(remote_message);
        }else if(chatting_room.chatroom_id!=chatroom_id){//채팅방에 있는데 알림온 채팅방과 현재 위치하고 있는 채팅방이 같은 채팅방 아니라면 알림 보냄
            SendNotification(remote_message);
        }else if(chatting_room.chatroom_id==chatroom_id && message_type==10){
            Intent intents = new Intent(this, call.class);
            intents.putExtra("friend_user_id", voice_user_id);
            intents.putExtra("call_type", 1);
            intents.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intents);
        }
    }

    private void SendNotification(RemoteMessage remote_message) {
        String chatroom_name = remote_message.getData().get("title"); // 채팅방 이름
        String chatroom_type = remote_message.getData().get("chatroom_type");
        String my_nickname = remote_message.getData().get("nickname");
        String new_chatroom_name = "";
        if(Objects.equals(chatroom_type, "3")){
            new_chatroom_name = chatroom_name;
        }else if(chatroom_type=="4"){
            new_chatroom_name = "그룹채팅";
        }
        else{
            String[] names = chatroom_name.split(", ");
            StringBuilder new_chatroomname = new StringBuilder();
            for (String name : names) {
                if (!name.equals(my_nickname)) {
                    if (new_chatroomname.length() > 0) {
                        new_chatroomname.append(", ");
                    }
                    new_chatroomname.append(name);
                }
            }
            new_chatroom_name = new_chatroomname.toString();
        }

        if(new_chatroom_name.length()>20){
            new_chatroom_name = new_chatroom_name.substring(0,20) + "...";
        }

        String sender = remote_message.getData().get("sender"); // 보낸사람 이름
        String body = remote_message.getData().get("body"); // 보낸 내용

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Chat Notifications";
            String description = "Notifications for chat messages";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notification_manager = getSystemService(NotificationManager.class);
            if (notification_manager != null) {
                notification_manager.createNotificationChannel(channel);
            }
        }
        
        Intent intent = new Intent(this, notification_check.class);
        intent.putExtra("chatroom_id",chatroom_id);

        if(message_type==10){
            intent.putExtra("message_type",message_type);
            intent.putExtra("friend_user_id",voice_user_id);
        }
        PendingIntent pending_intent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        RemoteViews remote_views = new RemoteViews(getPackageName(), R.layout.firebase_notification_layout);
        remote_views.setTextViewText(R.id.notification_title, new_chatroom_name);
        if(!Objects.equals(chatroom_type, "2")) {
            remote_views.setTextViewText(R.id.notification_sender, sender);
            remote_views.setTextViewText(R.id.notification_body, body);
        }else{
            remote_views.setTextViewText(R.id.notification_body, body);
            remote_views.setViewVisibility(R.id.notification_sender, View.GONE);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_profile_placeholder) // 알림 아이콘
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle()) // Custom view 스타일 적용
                .setCustomContentView(remote_views) // RemoteViews 적용
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pending_intent);

        NotificationManagerCompat notification_manager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notification_manager.notify(NOTIFICATION_ID, builder.build());
    }
}
