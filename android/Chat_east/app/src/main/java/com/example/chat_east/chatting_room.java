package com.example.chat_east;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Rect;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.chat_east.api.retrofit2_api_service;
import com.google.android.material.navigation.NavigationView;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.io.File;

import android.provider.DocumentsContract;
import android.content.ContentUris;
import okhttp3.ResponseBody;

import android.Manifest;


public class chatting_room extends AppCompatActivity {
    private Socket msocket;
    private chat_db_manager chat_db_manager;
    private friend_db_manager friend_db_manager;

    EditText msg_send;
    TextView chatroom_name;
    TextView people_count;
    public static final String user_id_key = "1";
    public static final String nickname_key = "";

    public static boolean inchatroom=false;

    private String my_user_id, friends_data, img_link1, img_link2, img_link3, img_link4;
    private int message_id1, message_id2, message_id3, message_id4;
    public static String my_nickname;
    public static int chatroom_id;
    private int chatroom_type;
    private boolean is_keyboard_open = false;
    private String chatting_room_name;

    private RecyclerView recycler_view, friend_recycler_view;
    private EditText write_text;
    private message_adapter message_adapter;
    private LinearLayout plus_container, voice_container;
    private DrawerLayout drawer_layout;
    private TextView sidebar_my_nickname;
    private ImageView chatroom_noti, chatroom_noti_deny, chatroom_favorite, chatroom_favorite_fill, sidebar_my_profile_img;
    private View header_view;

    private CardView card_view1, card_view2, card_view3, card_view4;
    private side_bar_friend_list_adapter side_bar_friend_list_adapter;

    boolean is_plusopen = false;
    boolean is_draweropen = false;
    boolean is_voiceopen = false;
    boolean is_recording = false;


    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        setContentView(R.layout.chatting_room);

        my_user_id = message_receive.user_id;
        my_nickname = message_receive.nickname;

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("chatroom_key")) {
            chatroom_id = intent.getIntExtra("chatroom_key",1);
        }
        message_receive.inchatroom = true;

        chat_db_manager = new chat_db_manager(this);
        friend_db_manager = new friend_db_manager(this);
        chat_db_manager.DeleteFakeMessage(chatroom_id);


        msg_send = findViewById(R.id.msg_send);
        chatting_room_name = chat_db_manager.GetChatroomName(chatroom_id);
        chatroom_name = findViewById(R.id.chatroom_name);
        chatroom_type = chat_db_manager.GetChatroomType(my_user_id,chatroom_id);
        if(chatroom_type==4){
            chatroom_name.setText("그룹채팅");
        }else if(chatroom_type == 2){
            String[] names = chatting_room_name.split(", ");
            StringBuilder new_chatroom_name = new StringBuilder();
            for (String name : names) {
                if (!name.equals(my_nickname)) {
                    if (new_chatroom_name.length() > 0) {
                        new_chatroom_name.append(", ");
                    }
                    new_chatroom_name.append(name);
                }
            }
            chatroom_name.setText(new_chatroom_name.toString());
        }else{
            chatroom_name.setText(chatting_room_name);
        }
        
        people_count = findViewById(R.id.people_count);
        people_count.setText(String.valueOf(chat_db_manager.GetPeopleCount(chatroom_id)));
        if(chatroom_type==1 || chatroom_type==2){
            people_count.setVisibility(View.GONE);
        }
        plus_container = findViewById(R.id.plus_container);
        drawer_layout = findViewById(R.id.drawer_layout);
        chatroom_noti = findViewById(R.id.chatroom_noti);
        chatroom_noti_deny = findViewById(R.id.chatroom_noti_deny);
        chatroom_favorite = findViewById(R.id.chatroom_favorite);
        chatroom_favorite_fill = findViewById(R.id.chatroom_favorite_fill);
        voice_container = findViewById(R.id.voice_container);

        NavigationView navigationView = findViewById(R.id.navigation_view);
        header_view = navigationView.getHeaderView(0);

        int favorite_type = chat_db_manager.GetFavoriteType(chatroom_id);
        int notification_type = chat_db_manager.GetNotificationType(chatroom_id);

        if(favorite_type==2){
            chatroom_favorite.setVisibility(View.GONE);
            chatroom_favorite_fill.setVisibility(View.VISIBLE);
        }

        if(notification_type==0){
            chatroom_noti.setVisibility(View.GONE);
            chatroom_noti_deny.setVisibility(View.VISIBLE);
        }


        friend_recycler_view = header_view.findViewById(R.id.join_people);

        card_view1 = header_view.findViewById(R.id.card_view1);
        card_view2 = header_view.findViewById(R.id.card_view2);
        card_view3 = header_view.findViewById(R.id.card_view3);
        card_view4 = header_view.findViewById(R.id.card_view4);
        sidebar_my_nickname = header_view.findViewById(R.id.sidebar_my_nickname);
        sidebar_my_profile_img = header_view.findViewById(R.id.sidebar_my_profile_image);
        SetMyProfile();

        ViewTreeObserver observer = card_view1.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int photo_count = SetPhoto(chat_db_manager.GetLastFourImage(chatroom_id));
                SetPeople(chat_db_manager.GetChatroomUser(chatroom_id,my_user_id));
                card_view1.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = card_view1.getWidth();
                SetImageHeight(card_view1,width);
                SetImageHeight(card_view2,width);
                SetImageHeight(card_view3,width);
                SetImageHeight(card_view4,width);
                card_view1.setVisibility(View.GONE);
                card_view2.setVisibility(View.GONE);
                card_view3.setVisibility(View.GONE);
                card_view4.setVisibility(View.GONE);
                if(photo_count>1){
                    card_view1.setVisibility(View.VISIBLE);
                    card_view2.setVisibility(View.VISIBLE);
                    card_view3.setVisibility(View.VISIBLE);
                    card_view4.setVisibility(View.VISIBLE);
                }
            }
        });

        drawer_layout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {}
            @Override
            public void onDrawerOpened(@NonNull View drawerView) {}
            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                SideBarClose();
            }
            @Override
            public void onDrawerStateChanged(int newState) {}
        });

        final View activity_rootview = findViewById(android.R.id.content);
        activity_rootview.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                activity_rootview.getWindowVisibleDisplayFrame(r);
                int screen_height = activity_rootview.getRootView().getHeight();
                int keypad_height = screen_height - r.bottom;

                boolean is_keyboard_open = keypad_height > screen_height * 0.15; // 키보드가 화면의 15% 이상 차지하면
                if (is_keyboard_open && !chatting_room.this.is_keyboard_open) { // 키보드가 열렸고 이전에 닫혀 있었으면
                    ScrollToBottom();
                    chatting_room.this.is_keyboard_open = true; // 플래그 업데이트
                } else if (!is_keyboard_open && chatting_room.this.is_keyboard_open) { // 키보드가 닫히면
                    chatting_room.this.is_keyboard_open = false; // 플래그 업데이트
                }
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (recorder != null) {
                    StopRecording(null);
                }
                voice_container.setVisibility(View.GONE);
                findViewById(R.id.sendRecordingView_enable).setVisibility(View.GONE);
                findViewById(R.id.sendRecordingView).setVisibility(View.VISIBLE);

                if(filename!=null) {
                    File file = new File(filename);
                    if (file.exists()) {
                        file.delete();
                    }
                }
                if (is_plusopen) {
                    plus_container.setVisibility(View.GONE);
                    is_plusopen = false;
                } else if(is_draweropen) {
                    drawer_layout.closeDrawer(GravityCompat.END,true);
                    drawer_layout.setVisibility(View.GONE);
                    is_draweropen = false;
                } else if(is_voiceopen){
                    voice_container.setVisibility(View.GONE);
                    is_voiceopen = false;
                }
                else {
                    finish();
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                }
            }
        });

        EditText msg_send = findViewById(R.id.msg_send);
        msg_send.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    MsgClick(v);
                }
                return false;
            }
        });

        write_text = findViewById(R.id.msg_send);
        write_text.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent key_event) {
                if(i == KeyEvent.KEYCODE_ENTER && key_event.getAction() == KeyEvent.ACTION_DOWN) {
                    Send();
                    return true;
                }
                return false;
            }
        });

        msg_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScrollToBottom();
            }
        });

        drawer_layout.setVisibility(View.INVISIBLE);
        drawer_layout.openDrawer(GravityCompat.END,false);
        is_draweropen = true;

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                drawer_layout.closeDrawer(GravityCompat.END, false);
                drawer_layout.setVisibility(View.GONE);
                is_draweropen = false;
            }
        }, 500);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                message_receive.inchatroom = true;
            }
        }, 1000);


    }

    void Connect() {
        try {
            //msocket = IO.socket("http://10.0.2.2:3000/");//안드로이드 avd사용시 로컬 호스트는 이 주소사용
            msocket = IO.socket("http://34.22.99.247:3000/");
            msocket.connect();//위 주소로 연결

            msocket.on("enter_chatroom", new Emitter.Listener() {//서버가 msg_to_client이벤트 일으키면 실행
                @Override
                public void call(Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject data = (JSONObject) args[0];
                                friends_data = data.getString("friends_data");
                                Log.d("enter_chatroom event", friends_data);
                                if(!Objects.equals(friends_data, "[]")){
                                    friends_data = ((JSONObject) args[0]).toString();
                                    UpdateFriendsData(friends_data);
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

            msocket.on("msg_to_client", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject data = (JSONObject) args[0];
                                chat_db_manager.AddMessage(data.getString("user_id"), data.getInt("message_id"), data.getInt("chatroom_id"), data.getString("content"), data.getString("file_name"), data.getInt("message_type"), data.getInt("width"), data.getInt("height"), data.getInt("file_size"), data.getString("create_time"), data.getString("update_time")); // 메시지를 데이터베이스에 저장
                                int message_id = data.getInt("message_id");
                                ChangeAdapter(message_id);
                                if(data.getInt("message_type")==2){
                                    Cursor cursor1 = chat_db_manager.GetLastFourImage(chatroom_id);
                                    SetPhoto(cursor1);
                                }
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });

            msocket.on("get_message", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject data = (JSONObject) args[0];
                                String message_data = data.getString("message_list");
                                Log.d("message_list",message_data);
                                if(!Objects.equals(message_data, "null")){
                                    message_data = ((JSONObject) args[0]).toString();
                                    UpdateMessage(message_data);
                                }
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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

    void EnterChatroom() {//메세지 전송 함수
        if (msocket != null) {//연결된경우에만 보내기 가능
            JSONObject data = new JSONObject();//서버에게 줄 데이터를 json으로 만든다
            try {
                data.put("user_id", my_user_id);
                data.put("chatroom_id", chatroom_id);//위에서 만든 json에 키와 값을 넣음
                data.put("update_time", chat_db_manager.GetLastUserTime(my_user_id, chatroom_id));
                msocket.emit("enter_chatroom", data);//서버에게 msg 이벤트 일어나게 함
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();

            }
        }
    }

    void LastMessageTime() {//메세지 전송 함수
        Log.d("Lastmessage_event","LastMessage");
        if (msocket != null) {//연결된경우에만 보내기 가능
            JSONObject data = new JSONObject();//서버에게 줄 데이터를 json으로 만든다
            try {
                data.put("chatroom_id", chatroom_id);//위에서 만든 json에 키와 값을 넣음
                data.put("update_time", chat_db_manager.GetChatroomLastTime(chatroom_id));
                msocket.emit("get_message", data);//서버에게 msg 이벤트 일어나게 함
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    private void UpdateMessage(String messages) {
        try {
            JSONObject jsonobject = new JSONObject(messages);
            JSONArray message_array = jsonobject.getJSONArray("message_list");
            for (int i = 0; i < message_array.length(); i++) {
                JSONObject message = message_array.getJSONObject(i);
                String user_id = message.getString("user_id");
                int message_id = message.getInt("message_id");
                if(chat_db_manager.MessageCheck(message_id))continue;
                int chatroom_id = message.getInt("chatroom_id");
                String content = message.getString("content");
                String file_name = message.getString("file_name");
                int message_type = message.getInt("message_type");
                int width = message.getInt("width");
                int height = message.getInt("height");
                int file_size = message.getInt("file_size");
                String create_time = message.getString("create_time");
                String update_time = message.getString("update_time");
                chat_db_manager.AddMessage(user_id, message_id, chatroom_id, content, file_name, message_type, width, height, file_size, create_time, update_time);
                ChangeAdapter(message_id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void ChangeAdapter(int message_id){
        Cursor new_cursor = chat_db_manager.GetMessageById(message_id);
        message_adapter.AddMessages(new_cursor);
        new_cursor.close();
        ScrollToBottom();
    }

    void Send() {//메세지 전송 함수
        if (msocket != null) {//연결된경우에만 보내기 가능
            JSONObject data = new JSONObject();//서버에게 줄 데이터를 json으로 만든다
            try {
                if(!msg_send.getText().toString().trim().isEmpty()) {
                    data.put("user_id", my_user_id);
                    data.put("chatroom_id", chatroom_id);//위에서 만든 json에 키와 값을 넣음
                    data.put("content", msg_send.getText().toString());
                    data.put("message_type", "1");
                    data.put("nickname", my_nickname);
                    data.put("chatroom_name",chatting_room_name);
                    Log.d("content",msg_send.getText().toString());
                    msocket.emit("msg", data);//서버에게 msg 이벤트 일어나게 함
                    msg_send.setText("");
                }
            } catch (Exception e) {

                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    public void MessageSend(View view){
        Send();
    }

    void MessageListShow(){
        recycler_view = findViewById(R.id.message_recyclerview);

        LinearLayoutManager layout_manager = new LinearLayoutManager(this);
        layout_manager.setStackFromEnd(true);

        Cursor message_cursor = chat_db_manager.GetDBMessage(chatroom_id);
        message_adapter = new message_adapter(this, message_cursor, chatroom_id, my_user_id, new message_adapter.OnItemClickListener() {
            @Override
            public void onItemClick(String user_id) {
                ViewProfile(user_id);
            }
        }, new message_adapter.PhotoClick() {
            @Override
            public void onItemClick(String img_url, int message_id) { ViewPhoto(img_url, message_id); }
        }, new message_adapter.FileDownload() {
            @Override
            public void onItemClick(String file_url, String file_name, int position) {
                DownloadFile(file_url, file_name,position);
            }
        }, new message_adapter.FileClick() {
            @Override
            public void onItemClick(String file_name) {
                FileClick(file_name);
            }
        }, new message_adapter.CallClick() {
            @Override
            public void onItemClick(String call_from_id, String call_to_id) {
                CallClick(call_from_id, call_to_id);
            }
        });

        recycler_view.setItemViewCacheSize(10);
        recycler_view.setLayoutManager(new LinearLayoutManager(this));
        recycler_view.setHasFixedSize(true); // RecyclerView의 크기가 고정되어 있을 때
        recycler_view.setDrawingCacheEnabled(true);
        recycler_view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recycler_view.setAdapter(message_adapter);
        recycler_view.scrollToPosition(message_adapter.getItemCount() - 1);
    }

    void CallClick(String call_from_id, String call_to_id){
        Log.d("CallClick","CallClick");
        if(Objects.equals(call_from_id, my_user_id)){//건 사람이 나라면
            Log.d("내가 검","내가검");
            Intent intent = new Intent(this, call.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("friend_user_id", call_to_id);
            Log.d("friend_user_id",call_to_id);
            intent.putExtra("call_type", 0);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }else{//건사람이 내가 아니면 나한테 걸려온것임
            Log.d("상대가 검","상대가검");
            Intent intent = new Intent(this, call.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("friend_user_id", call_from_id);
            intent.putExtra("call_type", 1);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    public void Call(View view){
        if(chatroom_type==2){
            Intent intent = new Intent(this, call.class);
            String friend_user_id = chat_db_manager.FindFriendID(my_user_id,chatroom_id);
            intent.putExtra("friend_user_id", friend_user_id);
            intent.putExtra("call_type", 0);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    void ViewPhoto(String img_url, int message_id){
        Intent intent = new Intent(this, photo_view.class);
        intent.putExtra("img_url", img_url);
        intent.putExtra("message_id", message_id);
        startActivity(intent);
    }

    void ViewProfile(String friend_user_id) {
        Intent profile = new Intent(this, profile_view.class);
        profile.putExtra(profile_view.view_user_id_key, friend_user_id);
        startActivity(profile);
    }

    void DownloadFile(String file_url,String file_name, int position){
        Log.d("file_url",file_url);
        Log.d("file_name",file_name);
        DownloadFile(this,file_url,file_name, position);
    }

    void FileClick(String file_name){
        String file_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/ChatEast/" + file_name;
        File file = new File(file_path);

        if (file.exists()) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            String ext = file.getName().substring(file.getName().lastIndexOf(".") + 1);
            String type = mime.getMimeTypeFromExtension(ext);

            Uri fileUri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", file);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, type);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            this.startActivity(intent);
        }
    }


    public void DownloadFile(Context context, String file_url, String file_name, int position) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(file_url);

        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle("Downloading " + file_name);
        request.setDescription("Downloading file...");

        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        String file_path = Environment.DIRECTORY_DOWNLOADS + "/ChatEast/" + file_name;
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "ChatEast" + "/" + file_name);

        if (downloadManager != null) {
            long downloadId = downloadManager.enqueue(request);  // 다운로드 ID 저장
            Log.d("Download", "Download started: " + file_path);

            BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    if (id == downloadId) {
                        // 다운로드 완료 시 실행되는 코드
                        Log.d("Download", "Download completed for file: " + file_name + " at position: " + position);
                        if(position!=-1){
                            message_adapter.notifyItemChanged(position);
                        }
                        context.unregisterReceiver(this);
                    }
                }
            };
            context.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        } else {
            Log.d("Download", "DownloadManager is null");
        }
    }

    public void ChatroomImg(View view){
        Intent profile = new Intent(this, chatroom_img.class);
        profile.putExtra("chatroom_key", chatroom_id);
        startActivity(profile);
    }

    void ViewTimeSend() {//메세지 전송 함수
        if (msocket != null) {//연결된경우에만 보내기 가능
            JSONObject data = new JSONObject();//서버에게 줄 데이터를 json으로 만든다
            try {
                data.put("user_id", my_user_id);
                data.put("chatroom_id", chatroom_id);//위에서 만든 json에 키와 값을 넣음
                msocket.emit("last_view_time", data);//서버에게 msg 이벤트 일어나게 함
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    void ScrollToBottom() {
        recycler_view.post(new Runnable() {
            @Override
            public void run() {
                int itemCount = recycler_view.getAdapter().getItemCount();
                if (itemCount > 0) {
                    recycler_view.scrollToPosition(itemCount - 1);
                }
            }
        });
    }

    void UpdateFriendsData(String friends_data) {
        try {
            JSONObject jsonobject = new JSONObject(friends_data);
            JSONArray chatroom_friend_array = jsonobject.getJSONArray("friends_data");
            for (int i = 0; i < chatroom_friend_array.length(); i++) {
                JSONObject chatroom_user_json = chatroom_friend_array.getJSONObject(i);
                String user_id = chatroom_user_json.getString("user_id");
                String nickname = chatroom_user_json.getString("nickname");
                String comment = chatroom_user_json.getString("comment");
                String profile_img_url = chatroom_user_json.getString("profile_img_url");
                String background_img_url = chatroom_user_json.getString("background_img_url");
                String update_time = chatroom_user_json.getString("update_time");
                int friends_type = chatroom_user_json.getInt("friends_type");
                int join_type = chatroom_user_json.getInt("join_type");
                chat_db_manager.AddChatroomUser(my_user_id, user_id, nickname, chatroom_id, friends_type, join_type, comment, profile_img_url, background_img_url, update_time);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        MessageListShow();
        ScrollToBottom();
        Cursor cursor = chat_db_manager.GetChatroomUser(chatroom_id,my_user_id);
        SetPeople(cursor);
    }

    public void OpenSideBar(View view){
        if(is_plusopen){
            plus_container.setVisibility(View.GONE);
            is_plusopen = false;
        }
        if(!is_draweropen) {
            HideKeyboard(view, msg_send,0);
            drawer_layout.setVisibility(View.VISIBLE);
            drawer_layout.openDrawer(GravityCompat.END, true);
            is_draweropen = true;
        }
    }

    public void SideBarClose(){
        drawer_layout.setVisibility(View.GONE);
        is_draweropen = false;
    }

    public void InviteFriend(View view){
        Intent chatroom_invite = new Intent(this, chatroom_invite.class);
        chatroom_invite.putExtra("chatroom_id", chatroom_id);
        startActivity(chatroom_invite);
    }

    public void MyProfile(View view){
        Log.d("my profile click","내 프로필 클릭");
        Intent profile = new Intent(this, profile_view.class);
        profile.putExtra(profile_view.view_user_id_key, my_user_id);
        startActivity(profile);
    }

    public void NotificationOn(View view){
        chatroom_noti_deny.setVisibility(View.GONE);
        chatroom_noti.setVisibility(View.VISIBLE);
        chat_db_manager.UpdateNotificationType(my_user_id,chatroom_id,1);
        if (msocket != null) {//연결된경우에만 보내기 가능
            JSONObject data = new JSONObject();//서버에게 줄 데이터를 json으로 만든다
            try {
                data.put("user_id", my_user_id);
                data.put("chatroom_id", chatroom_id);//위에서 만든 json에 키와 값을 넣음
                data.put("notification_type",1);
                msocket.emit("notification_type_update", data);//서버에게 msg 이벤트 일어나게 함
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    public void NotificationOff(View view){
        chatroom_noti.setVisibility(View.GONE);
        chatroom_noti_deny.setVisibility(View.VISIBLE);
        chat_db_manager.UpdateNotificationType(my_user_id,chatroom_id,0);
        if (msocket != null) {//연결된경우에만 보내기 가능
            JSONObject data = new JSONObject();//서버에게 줄 데이터를 json으로 만든다
            try {
                data.put("user_id", my_user_id);
                data.put("chatroom_id", chatroom_id);//위에서 만든 json에 키와 값을 넣음
                data.put("notification_type",0);
                msocket.emit("notification_type_update", data);//서버에게 msg 이벤트 일어나게 함
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }


    public void AddChatroomFavorite(View view){
        chatroom_favorite.setVisibility(View.GONE);
        chatroom_favorite_fill.setVisibility(View.VISIBLE);
        chat_db_manager.UpdateFavoriteType(my_user_id,chatroom_id,2);
        if (msocket != null) {//연결된경우에만 보내기 가능
            JSONObject data = new JSONObject();//서버에게 줄 데이터를 json으로 만든다
            try {
                data.put("user_id", my_user_id);
                data.put("chatroom_id", chatroom_id);//위에서 만든 json에 키와 값을 넣음
                data.put("favorite_type",2);
                msocket.emit("favorite_type_update", data);//서버에게 msg 이벤트 일어나게 함
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    public void DeleteChatroomFavorite(View view){
        chatroom_favorite_fill.setVisibility(View.GONE);
        chatroom_favorite.setVisibility(View.VISIBLE);
        chat_db_manager.UpdateFavoriteType(my_user_id,chatroom_id,1);
        if (msocket != null) {//연결된경우에만 보내기 가능
            JSONObject data = new JSONObject();//서버에게 줄 데이터를 json으로 만든다
            try {
                data.put("user_id", my_user_id);
                data.put("chatroom_id", chatroom_id);//위에서 만든 json에 키와 값을 넣음
                data.put("favorite_type",1);
                msocket.emit("favorite_type_update", data);//서버에게 msg 이벤트 일어나게 함
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    public void ExitChatroom(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if(chatroom_type==4){
            builder.setTitle("그룹채팅");
        }else if(chatroom_type == 2){
            String[] names = chatting_room_name.split(", ");
            StringBuilder new_chatroom_name = new StringBuilder();
            for (String name : names) {
                if (!name.equals(my_nickname)) {
                    if (new_chatroom_name.length() > 0) {
                        new_chatroom_name.append(", ");
                    }
                    new_chatroom_name.append(name);
                }
            }
            builder.setTitle(new_chatroom_name.toString());
        }else{
            builder.setTitle(chatting_room_name);
        }
        builder.setMessage("채팅방을 나가시겠어요?");

        builder.setPositiveButton("나가기", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (msocket != null) {//연결된경우에만 보내기 가능
                    JSONObject data = new JSONObject();//서버에게 줄 데이터를 json으로 만든다
                    try {
                        data.put("user_id", my_user_id);
                        data.put("chatroom_id", chatroom_id);//위에서 만든 json에 키와 값을 넣음
                        msocket.emit("leave_chatroom", data);//서버에게 msg 이벤트 일어나게 함
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
                chat_db_manager.LeaveChatroom(my_user_id, chatroom_id);
                finish(); // 현재 액티비티 종료
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // 다이얼로그 닫기
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private int SetPhoto(Cursor cursor){
        int a = 1;
        while (cursor.moveToNext()) {
            int message_index = cursor.getColumnIndex("message_id");
            int message_id = cursor.getInt(message_index);
            int content_index = cursor.getColumnIndex("content");
            String content = cursor.getString(content_index);

            @SuppressLint("SdCardPath")
            String image_path = "/data/data/com.example.chat_east/cache/C_E_preview/preview_" + String.valueOf(message_id) + ".jpg";

            if(a==1){img_link1 = content; message_id1 = message_id;  }
            else if(a==2){img_link2 = content; message_id2 = message_id;  }
            else if(a==3){img_link3 = content; message_id3 = message_id;  }
            else if(a==4){img_link4 = content; message_id4 = message_id;  }

            int image_view_id = getResources().getIdentifier("image_view" + a, "id", getPackageName());
            ImageView image_view = header_view.findViewById(image_view_id);

            File image_file = new File(image_path);

            if (!image_file.exists()) {
                new save_image_task(this, message_id, new save_image_task.SaveImageCallback() {
                    @Override
                    public void Onimagesaved(int messages_id, String image_path) {
                        if (messages_id == message_id) {
                            Glide.with(chatting_room.this)
                                    .load(image_path)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL) // 캐싱 전략 설정
                                    .into(image_view);
                        }
                    }
                }).execute(content, "preview_" + message_id + ".jpg");
            } else {
                Glide.with(this)
                        .load(image_path)
                        .diskCacheStrategy(DiskCacheStrategy.ALL) // 캐싱 전략 설정
                        .into(image_view);
            }
            a++;
        }
        cursor.close();
        if(a>1){
            card_view1.setVisibility(View.VISIBLE);
            card_view2.setVisibility(View.VISIBLE);
            card_view3.setVisibility(View.VISIBLE);
            card_view4.setVisibility(View.VISIBLE);
        }
        return a;
    }

    public void ViewImage1(View view){  ViewPhoto(img_link1, message_id1);   }
    public void ViewImage2(View view){  ViewPhoto(img_link2, message_id2);   }
    public void ViewImage3(View view){  ViewPhoto(img_link3, message_id3);   }
    public void ViewImage4(View view){  ViewPhoto(img_link4, message_id4);   }

    void SetPeople(Cursor cursor){
        Log.d("setpeople","on");
        friend_recycler_view.setLayoutManager(new LinearLayoutManager(this));
        side_bar_friend_list_adapter = new side_bar_friend_list_adapter(this, cursor, new side_bar_friend_list_adapter.OnItemClickListener() {
            @Override
            public void onItemClick(String user_id) {
                ViewProfile(user_id);
            }
        }, new side_bar_friend_list_adapter.Addfriend() {
            @Override
            public void Addfriend(String friend_id) {
                AddFriends(friend_id);
            }
        });
        friend_recycler_view.setAdapter(side_bar_friend_list_adapter);
    }

    void SetMyProfile(){
        Cursor cursor = friend_db_manager.ShowProfile(my_user_id);
        if(cursor.moveToFirst()){
            String nickname = cursor.getString(cursor.getColumnIndex("nickname"));
            String profile_img_url = cursor.getString(cursor.getColumnIndex("profile_img_url"));
            sidebar_my_nickname.setText(nickname);
            @SuppressLint("SdCardPath")
            String image_path = "/data/data/com.example.chat_east/cache/C_E_preview/preview_" + ExtractFileNameFromURL(profile_img_url) + ".jpg";
            File image_file = new File(image_path);
            ImageView android_layout = findViewById(R.id.android_layout);

            if (!image_file.exists()) {
                new save_image_task_profile(this, profile_img_url, new save_image_task_profile.SaveImageCallback() {
                    @Override
                    public void Onimagesaved(String profile_img_urls, String image_path) {
                        if (profile_img_url == profile_img_urls) {
                            Glide.with(chatting_room.this)
                                    .load(image_path)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL) // 캐싱 전략 설정
                                    .into(sidebar_my_profile_img);
                        }
                    }
                }).execute(profile_img_url, "preview_" + ExtractFileNameFromURL(profile_img_url) + ".jpg");
            } else {
                Glide.with(this)
                        .load(image_path)
                        .diskCacheStrategy(DiskCacheStrategy.ALL) // 캐싱 전략 설정
                        .into(sidebar_my_profile_img);
            }
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

    public void AddFriends(String friend_id){
        Log.d("add_friend","add_friend clicked");
        if (msocket != null) {
            JSONObject data = new JSONObject();
            try {
                data.put("from_user_id", my_user_id);
                data.put("to_user_id",friend_id);
                msocket.emit("add_friend", data);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
        Cursor cursor = chat_db_manager.GetFriendInfo(my_user_id, friend_id);
        if(cursor.moveToFirst()) {
            String nickname = cursor.getString(cursor.getColumnIndexOrThrow("nickname"));
            String comment = cursor.getString(cursor.getColumnIndexOrThrow("comment"));
            String profile_img_url = cursor.getString(cursor.getColumnIndexOrThrow("profile_img_url"));
            String background_img_url = cursor.getString(cursor.getColumnIndexOrThrow("background_img_url"));
            String update_time = cursor.getString(cursor.getColumnIndexOrThrow("update_time"));
            friend_db_manager.FriendUpdate(my_user_id, friend_id, nickname, comment, 1, profile_img_url, background_img_url, update_time);
        }
        cursor.close();
    }

    private void SetImageHeight(CardView image_view, int width) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) image_view.getLayoutParams();
        params.height = width;
        image_view.setLayoutParams(params);
    }

    ////////////////////////////////////////////////////////////////////////////image upload
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int STORAGE_PERMISSION_CODE = 2;
    private static final int PICK_FILE_REQUEST = 3;


    public void PlusClick(View view) {
        if(is_draweropen){
            drawer_layout.closeDrawer(GravityCompat.END,true);
            drawer_layout.setVisibility(View.GONE);
            is_draweropen = false;
        }
        if(!is_plusopen) {
            HideKeyboard(view, plus_container,1);
            is_plusopen = true;
        }
        if(is_voiceopen){
            voice_container.setVisibility(View.GONE);
        }
    }

    public void MsgClick(View view) {
        Log.d("asdasd","message click");
        if(is_plusopen) {
            plus_container.setVisibility(View.GONE);
            is_plusopen = false;
        }

    }

    public void Back(View view){
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    public void HideKeyboard(final View view_hide, final View plus_containers, int a) {
        InputMethodManager imm = (InputMethodManager) view_hide.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view_hide.getWindowToken(), 0);
        }

        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(300); // Duration in milliseconds
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                plus_containers.setVisibility(View.VISIBLE);
                AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                fadeIn.setDuration(300); // Duration in milliseconds
                plus_containers.startAnimation(fadeIn);
                if(a==1) ScrollToBottom();
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        view_hide.startAnimation(fadeOut);
    }

    public void GalleryClick(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                OpenGallery();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, STORAGE_PERMISSION_CODE);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                OpenGallery();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
            }
        }
    }


    private void OpenGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int request_code, int result_code, Intent data) {
        super.onActivityResult(request_code, result_code, data);
        if (request_code == PICK_FILE_REQUEST && result_code == RESULT_OK && data != null && data.getData() != null) {
            Uri file_uri = data.getData();
            UploadFile(file_uri);
        } else if (request_code == PICK_IMAGE_REQUEST && result_code == RESULT_OK && data != null && data.getData() != null) {
            Uri image_uri = data.getData();
            UploadImage(image_uri);
        }
    }

    private void UploadImage(Uri image_uri) {
        try {
            String file_path = GetRealPathFromURI(image_uri);
            File file = new File(file_path);

            RequestBody request_file = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), request_file);

            RequestBody user_id = RequestBody.create(MediaType.parse("multipart/form-data"), my_user_id);
            RequestBody chatroom_id = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(this.chatroom_id));
            RequestBody message_type = RequestBody.create(MediaType.parse("multipart/form-data"), "2"); // 예를 들어 메시지 타입을 1로 설정
            RequestBody nickname = RequestBody.create(MediaType.parse("multipart/form-data"), my_nickname);
            RequestBody chatroom_name = RequestBody.create(MediaType.parse("multipart/form-data"), chatting_room_name);
            RequestBody path_type = RequestBody.create(MediaType.parse("multipart/form-data"), "1");
            RequestBody real_file_name = RequestBody.create(MediaType.parse("multipart/form-data"), file.getName());

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://34.22.99.247:3000/")
                    .client(new OkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            retrofit2_api_service service = retrofit.create(retrofit2_api_service.class);
            Call<ResponseBody> call = service.uploadFile(body, user_id, chatroom_id, message_type, nickname, chatroom_name, path_type, real_file_name);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(chatting_room.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(chatting_room.this, "Upload failed: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(chatting_room.this, "Upload error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String GetRealPathFromURI(Uri uri) {
        String path = null;

        // Document 타입의 URI인지 확인
        if (DocumentsContract.isDocumentUri(this, uri)) {
            String doc_id = DocumentsContract.getDocumentId(uri);
            String[] split = doc_id.split(":");
            String type = split[0];

            if ("image".equals(type)) {
                Uri content_uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selection_args = new String[]{split[1]};
                Cursor cursor = getContentResolver().query(content_uri, new String[]{MediaStore.Images.Media.DATA}, selection, selection_args, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                    }
                    cursor.close();
                }
            } else if ("video".equals(type)) {
                Uri content_uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                String selection = MediaStore.Video.Media._ID + "=?";
                String[] selection_args = new String[]{split[1]};
                Cursor cursor = getContentResolver().query(content_uri, new String[]{MediaStore.Video.Media.DATA}, selection, selection_args, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                    }
                    cursor.close();
                }
            } else if ("audio".equals(type)) {
                Uri content_uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String selection = MediaStore.Audio.Media._ID + "=?";
                String[] selection_args = new String[]{split[1]};
                Cursor cursor = getContentResolver().query(content_uri, new String[]{MediaStore.Audio.Media.DATA}, selection, selection_args, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    }
                    cursor.close();
                }
            } else if (uri.getAuthority().equals("com.android.providers.downloads.documents")) {
                Uri content_uri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(doc_id));
                path = GetDataColumn(content_uri, null, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            path = GetDataColumn(uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            path = uri.getPath();
        }

        return path;
    }

    private String GetDataColumn(Uri uri, String selection, String[] selection_args) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = getContentResolver().query(uri, projection, selection, selection_args, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int request_code, @NonNull String[] permissions, @NonNull int[] grant_results) {
        super.onRequestPermissionsResult(request_code, permissions, grant_results);
        if (request_code == STORAGE_PERMISSION_CODE) {
            if (grant_results.length > 0 && grant_results[0] == PackageManager.PERMISSION_GRANTED) {
                OpenGallery();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void FileClick(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                OpenFilePicker();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, STORAGE_PERMISSION_CODE);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                OpenFilePicker();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
            }
        }
    }

    private void OpenFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // 모든 파일 타입 선택 가능
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    private void UploadFile(Uri file_uri) {
        try {
            String file_path = GetRealPathFromURI(file_uri);
            Log.d("file path", "" + file_path);
            File file = new File(file_path);

            if (file.length() > 100L * 1024 * 1024) {
                Toast.makeText(this, "파일의 크기가 너무 큽니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            RequestBody request_file = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), request_file);

            RequestBody user_id = RequestBody.create(MediaType.parse("multipart/form-data"), my_user_id);
            RequestBody chatroom_id = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(this.chatroom_id));
            RequestBody message_type = RequestBody.create(MediaType.parse("multipart/form-data"), "4");
            RequestBody nickname = RequestBody.create(MediaType.parse("multipart/form-data"), my_nickname);
            RequestBody chatroom_name = RequestBody.create(MediaType.parse("multipart/form-data"), chatting_room_name);
            RequestBody path_type = RequestBody.create(MediaType.parse("multipart/form-data"), "6");
            RequestBody real_file_name = RequestBody.create(MediaType.parse("multipart/form-data"), file.getName());

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://34.22.99.247:6001/")
                    .client(new OkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            retrofit2_api_service service = retrofit.create(retrofit2_api_service.class);
            Call<ResponseBody> call = service.uploadFile(body, user_id, chatroom_id, message_type, nickname, chatroom_name, path_type, real_file_name);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(chatting_room.this, "File uploaded successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(chatting_room.this, "Upload failed: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(chatting_room.this, "Upload error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void UploadAudio(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }

        if (file.length() > 100L * 1024 * 1024) {
            Toast.makeText(this, "파일의 크기가 너무 큽니다.", Toast.LENGTH_SHORT).show();
            file.delete();
            return;
        }

        RequestBody request_file = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), request_file);

        RequestBody user_id = RequestBody.create(MediaType.parse("multipart/form-data"), my_user_id);
        RequestBody chatroom_id = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(this.chatroom_id));
        RequestBody message_type = RequestBody.create(MediaType.parse("multipart/form-data"), "5");
        RequestBody nickname = RequestBody.create(MediaType.parse("multipart/form-data"), my_nickname);
        RequestBody chatroom_name = RequestBody.create(MediaType.parse("multipart/form-data"), chatting_room_name);
        RequestBody path_type = RequestBody.create(MediaType.parse("multipart/form-data"), "7");
        RequestBody real_file_name = RequestBody.create(MediaType.parse("multipart/form-data"), file.getName());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://34.22.99.247:6001/")
                .client(new OkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofit2_api_service service = retrofit.create(retrofit2_api_service.class);
        Call<ResponseBody> call = service.uploadFile(body, user_id, chatroom_id, message_type, nickname, chatroom_name, path_type, real_file_name);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(chatting_room.this, "음성 메시지 업로드 성공", Toast.LENGTH_SHORT).show();
                    file.delete();
                } else {
                    Toast.makeText(chatting_room.this, "업로드 실패: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(chatting_room.this, "업로드 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private MediaRecorder recorder;
    private String filename = null;
    private boolean is_recording_process_active = false;

    public void CancelVoice(View view){
        is_voiceopen = false;

        if (is_recording) {
            StopRecording(view);

            File previous_file = new File(filename);
            if (previous_file.exists()) {
                previous_file.delete();
            }
        }

        findViewById(R.id.sendRecordingView_enable).setVisibility(View.GONE);
        findViewById(R.id.sendRecordingView).setVisibility(View.VISIBLE);
        voice_container.setVisibility(View.GONE);
    }

    public void VoiceMessageClick(View view) {
        if(is_plusopen) {
            plus_container.setVisibility(View.GONE);
            is_plusopen = false;
        }
        is_voiceopen = true;
        Log.d("voice message","click");

        filename = getExternalCacheDir().getAbsolutePath();
        filename += "/audiorecord.3gp";

        voice_container.setVisibility(View.VISIBLE);
    }

    public void StartRecording(View view) {
        File previous_file = new File(filename);
        if (previous_file.exists()) {
            previous_file.delete();
        }

        if (is_recording_process_active) return;
        is_recording_process_active = true;
        // 권한 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    4);
            return;  // 권한이 없으면 녹음을 시작하지 않음
        }

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(filename);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e("VoiceMessage", "녹음 준비 실패", e);
            return;
        }

        recorder.start();
        is_recording = true;
        // 진폭 계산을 위해 AudioRecord 사용
        int sample_rate = 44100;
        int buffer_size = AudioRecord.getMinBufferSize(sample_rate,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        AudioRecord audio_record = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sample_rate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                buffer_size
        );

        short[] buffer = new short[buffer_size];
        audio_record.startRecording();

        new Thread(() -> {
            while (is_recording) {
                int read = audio_record.read(buffer, 0, buffer_size);
                if (read > 0) {
                    final int[] maxAmplitude = {0};
                    for (int i = 0; i < read; i++) {
                        maxAmplitude[0] = Math.max(maxAmplitude[0], Math.abs(buffer[i]));
                    }

                    AmplitudeView amplitudeView = findViewById(R.id.amplitudeView);

                    runOnUiThread(() -> {
                        amplitudeView.setAmplitude(maxAmplitude[0]);
                    });
                }
            }

            audio_record.stop();
            audio_record.release();
        }).start();

        findViewById(R.id.stopRecordingView).setVisibility(View.VISIBLE);
        findViewById(R.id.stopRecordingcardView).setVisibility(View.VISIBLE);
        findViewById(R.id.startRecordingView).setVisibility(View.GONE);
        is_recording_process_active = false;
    }

    public void StopRecording(View view) {
        is_recording_process_active = true;
        is_recording = false;

        if (recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;

            Toast.makeText(this, "녹음 중지", Toast.LENGTH_SHORT).show();

            findViewById(R.id.sendRecordingView).setVisibility(View.GONE);
            findViewById(R.id.sendRecordingView_enable).setVisibility(View.VISIBLE);

            findViewById(R.id.stopRecordingView).setVisibility(View.GONE);
            findViewById(R.id.stopRecordingcardView).setVisibility(View.GONE);
            findViewById(R.id.startRecordingView).setVisibility(View.VISIBLE);
        }

        is_recording_process_active = false;
    }

    public void SendRecording(View view) {
        StopRecording(view);
        is_voiceopen =false;
        findViewById(R.id.sendRecordingView).setVisibility(View.VISIBLE);
        findViewById(R.id.sendRecordingView_enable).setVisibility(View.GONE);
        voice_container.setVisibility(View.GONE);
        UploadAudio(filename);
    }
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++

    ////////////////////////////////////////////////////////////////////////////image upload

    @Override
    protected void onDestroy() {//어플리케이션 종료시 실행
        super.onDestroy();
        Log.d("onDestroy","active");
        message_receive.inchatroom = false;
        msocket.disconnect();
        ViewTimeSend();
        chat_db_manager.close();
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    protected void onPause() {
        super.onPause();
        ViewTimeSend();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResume(){
        super.onResume();
        if(msocket==null){
            Connect();
            EnterChatroom();
            MessageListShow();
            LastMessageTime();
        }
        else if(!msocket.connected()){
            Connect();
            EnterChatroom();
            LastMessageTime();
        }
        LastMessageTime();
    }

    @Override
    public void onRestart(){
        super.onRestart();
        SetMyProfile();
        Cursor cursor = chat_db_manager.GetChatroomUser(chatroom_id,my_user_id);
        SetPeople(cursor);
    }
}