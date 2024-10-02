package com.example.chat_east;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.android.gms.tasks.OnCompleteListener;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Objects;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class login_check extends AppCompatActivity {
    private Socket msocket;
    private static final String tag = "LoginFragment";
    private static final int rc_sign_in = 9001;
    private String user_email, email_company, my_user_id, friend_user_id, recent_messages, my_nickname, profile_data, my_user_tag, fcm_token;
    private int chatroom_id;

    private GoogleSignInClient google_signin_client;
    private GoogleSignInAccount gsa;
    private FirebaseAuth m_auth;
    private chat_db_manager chat_db_manager;
    private friend_db_manager friend_db_manager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chat_db_manager = new chat_db_manager(this);
        friend_db_manager = new friend_db_manager(this);

        GetFcmToken();
        GetAccessNC();
        GetAccessPhoto();
        GetAccessVoice();
        RequestAudioPermissions();


        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("chatroom_id")) {
            chatroom_id = intent.getIntExtra("chatroom_id",0);
        }
        if (intent != null && intent.hasExtra("friend_user_id")) {
            friend_user_id = intent.getStringExtra("friend_user_id");
        }

        m_auth = FirebaseAuth.getInstance();

        GoogleSignInOptions google_signin_options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        google_signin_client = GoogleSignIn.getClient(this, google_signin_options);

        gsa = GoogleSignIn.getLastSignedInAccount(login_check.this);
        if (gsa != null) { // 로그인 되있는 경우
            SignIn();
        }else{
            Intent login_intent = new Intent(getApplicationContext(), login_activity.class);
            startActivity(login_intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }
    }
    void LoginSuccess(){//로그인 성공시 이벤트
        try{
            //msocket = IO.socket("http://10.0.2.2:3000/");//안드로이드 avd사용시 로컬 호스트는 이 주소사용
            msocket = IO.socket("http://34.22.99.247:3000/");
            msocket.connect();
            JSONObject data = new JSONObject();//서버에게 줄 데이터를 json으로 만든다
            data.put("Login", "Success");
            data.put("email",user_email);
            data.put("email_company",email_company);
            data.put("nickname", my_nickname);
            data.put("fcm_token",fcm_token);
            Log.d("fcm_token",fcm_token);
            Cursor cursor = chat_db_manager.GetLastTime();
            if (cursor.moveToFirst()) {
                String msg_last_time = cursor.getString(cursor.getColumnIndexOrThrow("create_time"));
                data.put("last_message_time",msg_last_time);
            }else{
                data.put("last_message_time","2020-01-01 00:00:00");
            }
            msocket.emit("login_success", data);
            msocket.on("get_message", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject data = (JSONObject) args[0];
                                my_user_id = data.getString("user_id");
                                JSONObject friend_data = new JSONObject();
                                friend_data.put("user_id",my_user_id);
                                Log.d("user_id", my_user_id);
                                Cursor cursor2 = friend_db_manager.GetProfileTime(my_user_id);
                                if (cursor2.moveToFirst()) {
                                    String last_time = cursor2.getString(cursor2.getColumnIndexOrThrow("update_time"));
                                    friend_data.put("update_time",last_time);
                                }else{
                                    friend_data.put("update_time","2020-01-01 00:00:00");
                                }
                                cursor2.close();
                                msocket.emit("get_profile", friend_data);

                                recent_messages = data.getString("recent_messages");
                                my_nickname = data.getString("nickname");
                                my_user_tag = data.getString("user_tag");
                                Log.d("get_message event",recent_messages);
                                if(!Objects.equals(recent_messages, "[]")){
                                    recent_messages = ((JSONObject) args[0]).toString();
                                    MessageSave(recent_messages);
                                }

                                Intent endservice = new Intent(getApplicationContext(),message_receive.class);
                                stopService(endservice);

                                Intent background_receive = new Intent(getApplicationContext(), message_receive.class);
                                background_receive.putExtra(message_receive.user_id_key, my_user_id);
                                background_receive.putExtra(message_receive.nickname_key, my_nickname);
                                background_receive.putExtra(message_receive.email_key, user_email);
                                background_receive.putExtra(message_receive.user_tag_key, my_user_tag);
                                startService(background_receive);
                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);


                                if(chatroom_id==0) {
                                    Intent friend_intent = new Intent(getApplicationContext(), main_friend.class);
                                    friend_intent.putExtra("friend_user_id", friend_user_id);
                                    startActivity(friend_intent);
                                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                }
                                else{
                                    Log.d("intent","chatroom move");
                                    Intent chatroom_intent = new Intent(getApplicationContext(), main_chatting.class);
                                    chatroom_intent.putExtra("chatroom_id",chatroom_id);
                                    startActivity(chatroom_intent);
                                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                }

                                finish();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
            msocket.on("get_profile", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject data = (JSONObject) args[0];
                                profile_data = data.getString("profile_data");
                                Log.d("my_profile",profile_data);
                                if(!Objects.equals(profile_data, "null")){
                                    profile_data = ((JSONObject) args[0]).toString();
                                    ProfileUpdate(profile_data);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
        }
        catch (Exception e) {
            Log.e("SOCKET", "Connection error", e);
            e.printStackTrace();
        }
    }

    private void GetFcmToken(){
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (task.isSuccessful()) {
                            fcm_token = task.getResult();
                            Log.d("FCM Token", "FCM 토큰: " + fcm_token);
                        } else {
                            Log.e("FCM Token", "FCM 토큰 가져오기 실패", task.getException());
                        }
                    }
                });
    }

    private void GetAccessNC(){//알림 허용
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        101);
            }
        }
    }

    private void GetAccessVoice(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 4);
        }
    }

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    // 권한 요청 함수
    private void RequestAudioPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 허용됨
            } else {
                // 권한이 거부됨
                Toast.makeText(this, "Audio permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void GetAccessPhoto() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_MEDIA_IMAGES},
                        101);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        101);
            }
        }
    }




    private void SignIn(){
        Intent sign_in_intent = google_signin_client.getSignInIntent();
        startActivityForResult(sign_in_intent, rc_sign_in);
    }

    @Override
    public void onActivityResult(int request_code, int result_code, Intent data) {
        super.onActivityResult(request_code, result_code, data);

        if (request_code == rc_sign_in) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            HandleSignInResult(task);
        }
    }

    private void HandleSignInResult(Task<GoogleSignInAccount> completetask) {//사용자 정보 획득
        try {
            GoogleSignInAccount acct = completetask.getResult(ApiException.class);

            if (acct != null) {
                FirebaseAuthWithGoogle(acct.getIdToken());

                String person_name = acct.getDisplayName();
                String person_given_name = acct.getGivenName();
                String person_family_name = acct.getFamilyName();
                String person_email = acct.getEmail();
                String person_id = acct.getId();
                Uri person_photo = acct.getPhotoUrl();

                user_email = person_email;
                email_company = "Google";
                my_nickname = person_name;

                LoginSuccess();
            }
        } catch (ApiException e) {
            Log.e(tag, "signInResult:failed code=" + e.getStatusCode());
        }
    }


    // [START auth_with_google]
    private void FirebaseAuthWithGoogle(String id_token) {
        AuthCredential credential = GoogleAuthProvider.getCredential(id_token, null);
        m_auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(tag, "signInWithCredential:success");
                        FirebaseUser user = m_auth.getCurrentUser();
                    } else {
                        Log.w(tag, "signInWithCredential:failure", task.getException());
                        Toast.makeText(login_check.this, R.string.failed_login, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void MessageSave(String messages) {
        try {
            JSONObject jsonobject = new JSONObject(messages);
            JSONArray message_array = jsonobject.getJSONArray("recent_messages");
            for (int i = 0; i < message_array.length(); i++) {
                JSONObject message = message_array.getJSONObject(i);
                String user_id = message.getString("user_id");
                int message_id = message.getInt("message_id");
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ProfileUpdate(String messages) {
        try {
            JSONObject jsonobject = new JSONObject(messages);
            JSONArray message_array = jsonobject.getJSONArray("profile_data");
            for (int i = 0; i < message_array.length(); i++) {
                JSONObject message = message_array.getJSONObject(i);
                String user_id = message.getString("user_id");
                String nick_name = message.getString("nickname");
                String comment = message.getString("comment");
                String profile_img_url =  message.getString("profile_img_url");
                String background_img_url =  message.getString("background_img_url");
                String update_time = message.getString("last_profile_update");

                friend_db_manager.SaveProfile(user_id, nick_name, comment, profile_img_url, background_img_url, update_time);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void SignOut() {//로그아웃
        google_signin_client.signOut()
                .addOnCompleteListener(this, task -> {
                    m_auth.signOut();
                    Toast.makeText(login_check.this, R.string.success_logout, Toast.LENGTH_SHORT).show();
                });
        gsa = null;
    }

    @Override
    protected void onStop() {//액티비티 종료시 실행
        super.onStop();
        if (msocket != null) {
            msocket.disconnect();//소켓을 닫는다
        }
        chat_db_manager.close();
        friend_db_manager.close();
    }

    @Override
    protected void onDestroy() {//어플리케이션 종료시 실행
        super.onDestroy();
        if (msocket != null) {
            msocket.disconnect(); // 소켓을 닫는다
        }
        chat_db_manager.close();
        friend_db_manager.close();
    }
}