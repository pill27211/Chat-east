package com.example.chat_east;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.chat_east.api.retrofit2_api_profile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.Objects;

import org.json.JSONException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class profile_view extends AppCompatActivity {
    private Socket msocket;
    private friend_db_manager friend_db_manager;
    private chat_db_manager chat_db_manager;

    public static final String user_id_key = "1";
    public static final String nickname_key = "";
    public static final String view_user_id_key = "";
    public String my_user_id;
    public String my_nickname;

    private String user_data, friend_user_id, friend_nickname, friend_comment, friend_profile_img_url, friend_background_img_url, friend_last_profile_update, new_nickname, new_comment, new_profile_img, new_background_img;
    private int edit_layout_count, image_upload_view_id;
    private boolean etc_layout_open=false;

    private TextView user_nickname, user_comment, nickname_character_count, comment_character_count, profile_edit_text, edit_confirm, self_chat_text, edit_text;
    private View etc_button, favorite_button_white, favorite_button_yellow, nickname_line, comment_line, comment_edit_back_button, self_chatting_icon, edit_icon, vector, nickname_edit_back_button;
    private LinearLayout self_chatting_layout, edit_layout, add_friend_layout, ignore_layout, chatting_layout, call_layout, video_call_layout, unignore_layout, etc_layout, profile_edit_camera;
    private ImageView comment_edit_icon, nickname_edit_icon, nickname_clear_text_button, comment_clear_text_button, profile_img, profile_camera;
    private EditText nickname_current_edit_text, comment_current_edit_text;
    private RelativeLayout nickname_edit_layout, comment_edit_layout;
    private Uri current_profile_image_uri, current_background_image_uri;

    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        setContentView(R.layout.profile_layout);

        my_user_id = message_receive.user_id;
        my_nickname = message_receive.nickname;

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(view_user_id_key)) {
            friend_user_id = intent.getStringExtra(view_user_id_key);
        }

        user_nickname = findViewById(R.id.user_nickname);
        user_comment = findViewById(R.id.user_comment);
        etc_button = findViewById(R.id.etc_button);
        favorite_button_white = findViewById(R.id.favorite_button);
        favorite_button_yellow = findViewById(R.id.favorite_button_yellow);
        self_chatting_layout = findViewById(R.id.self_chatting_layout);
        edit_layout = findViewById(R.id.edit_layout);
        add_friend_layout = findViewById(R.id.add_friend_layout);
        ignore_layout = findViewById(R.id.ignore_layout);
        chatting_layout = findViewById(R.id.chatting_layout);
        call_layout = findViewById(R.id.call_layout);
        video_call_layout = findViewById(R.id.video_call_layout);
        unignore_layout = findViewById(R.id.unignore_layout);
        etc_layout = findViewById(R.id.etc_layout);
        profile_edit_camera = findViewById(R.id.profile_edit_camera);
        profile_edit_text = findViewById(R.id.profile_edit_text);
        edit_confirm = findViewById(R.id.edit_confirm);
        comment_edit_icon = findViewById(R.id.comment_edit_icon);
        nickname_edit_icon = findViewById(R.id.nickname_edit_icon);
        nickname_edit_layout = findViewById(R.id.edit_nickname_layout);
        nickname_current_edit_text = findViewById(R.id.current_nickname);
        nickname_line = findViewById(R.id.nickname_line);
        nickname_edit_back_button = findViewById(R.id.edit_back_button);
        nickname_character_count = findViewById(R.id.character_count);
        nickname_clear_text_button = findViewById(R.id.clear_text_button);
        comment_edit_layout = findViewById(R.id.edit_comment_layout);
        comment_current_edit_text = findViewById(R.id.current_comment);
        comment_line = findViewById(R.id.comment_line);
        comment_edit_back_button = findViewById(R.id.edit_comment_back_button);
        comment_character_count = findViewById(R.id.comment_character_count);
        comment_clear_text_button = findViewById(R.id.clear_comment_text_button);
        self_chat_text = findViewById(R.id.self_chat_text);
        edit_text = findViewById(R.id.edit_text);
        self_chatting_icon = findViewById(R.id.self_chatting_icon);
        edit_icon = findViewById(R.id.edit_icon);
        vector = findViewById(R.id.vector);
        profile_img = findViewById(R.id.profile_img);
        profile_img.setOnClickListener(this::GalleryClick);
        profile_camera = findViewById(R.id.profile_camera);
        profile_camera.setOnClickListener(this::GalleryClick);

        edit_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(edit_layout_count != 0) {
                    return;
                }

                self_chat_text.setVisibility(View.INVISIBLE);
                edit_text.setVisibility(View.INVISIBLE);
                self_chatting_icon.setVisibility(View.INVISIBLE);
                edit_icon.setVisibility(View.INVISIBLE);
                vector.setVisibility(View.INVISIBLE);
                comment_edit_icon.setVisibility(View.VISIBLE);
                nickname_edit_icon.setVisibility(View.VISIBLE);
                nickname_line.setVisibility(View.VISIBLE);
                comment_line.setVisibility(View.VISIBLE);
                profile_edit_camera.setVisibility(View.VISIBLE);
                profile_edit_text.setVisibility(View.VISIBLE);
                edit_confirm.setVisibility(View.VISIBLE);
                profile_camera.setVisibility(View.VISIBLE);

                LinearLayout nickname_linear_layout = findViewById(R.id.nickname_linear);
                int original_padding_bottom = nickname_linear_layout.getPaddingBottom();
                int additional_padding_bottom = 15;
                nickname_linear_layout.setPadding(
                        nickname_linear_layout.getPaddingLeft(),
                        nickname_linear_layout.getPaddingTop(),
                        nickname_linear_layout.getPaddingRight(),
                        original_padding_bottom + additional_padding_bottom
                );
                edit_layout_count++;
            }
        });
        nickname_edit_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {// 닉네임 수정 레이아웃 표시
                nickname_edit_layout.setVisibility(View.VISIBLE);
                nickname_current_edit_text.setText(user_nickname.getText().toString()); // 현재 닉네임으로 채우기
                nickname_current_edit_text.requestFocus();
                nickname_edit_back_button.setVisibility(View.VISIBLE);
                edit_confirm.setVisibility(View.INVISIBLE);

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(nickname_current_edit_text, InputMethodManager.SHOW_IMPLICIT);

                int text_length = nickname_current_edit_text.getText().length();
                nickname_current_edit_text.setSelection(text_length);
            }
        });
        nickname_current_edit_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence char_sequence, int start, int count, int after) {    }
            @Override
            public void onTextChanged(CharSequence char_sequence, int start, int before, int count) {
                int length = char_sequence.length();
                nickname_character_count.setText(length + " / 20");
            }
            @Override
            public void afterTextChanged(Editable editable) {   }
        });
        nickname_clear_text_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nickname_current_edit_text.setText("");
            }
        });

        comment_edit_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {// 코멘트 수정 레이아웃 표시
                comment_edit_layout.setVisibility(View.VISIBLE);
                edit_confirm.setVisibility(View.INVISIBLE);
                comment_current_edit_text.setText(user_comment.getText().toString()); // 현재 코멘트로 채우기
                comment_current_edit_text.requestFocus();
                comment_edit_back_button.setVisibility(View.VISIBLE);

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(comment_current_edit_text, InputMethodManager.SHOW_IMPLICIT);

                int text_length = comment_current_edit_text.getText().length();
                comment_current_edit_text.setSelection(text_length);
            }
        });
        comment_current_edit_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence char_sequence, int start, int count, int after) {    }
            @Override
            public void onTextChanged(CharSequence char_sequence, int start, int before, int count) {
                int length = char_sequence.length();
                comment_character_count.setText(length + " / 64");
            }
            @Override
            public void afterTextChanged(Editable editable) {   }
        });
        comment_clear_text_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comment_current_edit_text.setText("");
            }
        });

        findViewById(R.id.main_layout).setOnTouchListener(new View.OnTouchListener() {//etc_lay아웃 바깥부분을 클릭하면 닫힘
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    int[] location = new int[2];
                    etc_layout.getLocationOnScreen(location);
                    float x = event.getRawX();
                    float y = event.getRawY();
                    if (x < location[0] || x > location[0] + etc_layout.getWidth() ||
                            y < location[1] || y > location[1] + etc_layout.getHeight()) {
                        if (etc_layout.getVisibility() == View.VISIBLE) {
                            etc_layout.setVisibility(View.INVISIBLE);
                            etc_layout_open = false;
                        }
                    }
                }
                return false;
            }
        });

        friend_db_manager = new friend_db_manager(this);
        chat_db_manager = new chat_db_manager(this);
        Connect();
        SearchUser();
        FriendTypeCheck();
    }

    @Override
    public void onBackPressed() {
        if (nickname_edit_layout.getVisibility() == View.VISIBLE || comment_edit_layout.getVisibility() == View.VISIBLE) {
            // 뒤로가기 버튼 시, 닉네임 수정 or 코멘트 수정 레이아웃이 보이는 상태라면 EditBackactivity() 실행
            EditBackActivity2(null);
        }
        else if(comment_edit_icon.getVisibility() == View.VISIBLE){
            EditBackActivity1(null);
        }
        else {
            super.onBackPressed();
        }
    }
    public void ConfirmEditNickname(View view) {
        String new_nickname = nickname_current_edit_text.getText().toString().trim();
        if (new_nickname.isEmpty()) {
            Toast.makeText(getApplicationContext(), "닉네임을 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        message_receive.nickname = new_nickname;
        this.new_nickname = new_nickname;
        user_nickname.setText(new_nickname);
        EditBackActivity2(null);
    }

    public void ConfirmEditComment(View view) {
        String new_comment = comment_current_edit_text.getText().toString().trim();
        this.new_comment=new_comment;
        user_comment.setText(new_comment);
        EditBackActivity2(null);
    }

    public void UpdateNickname(){
        if (msocket != null) {
            JSONObject data = new JSONObject();
            try {
                data.put("user_id", my_user_id);
                data.put("nickname", new_nickname);
                msocket.emit("update_nickname", data);
                user_nickname.setText(new_nickname);
                my_nickname=new_nickname;
                main_friend.my_nickname=new_nickname;
                friend_db_manager.ChangeNickname(my_user_id,new_nickname);
                chatting_room.my_nickname = new_nickname;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
        }
    }

    public void UpdateComment(){
        if (msocket != null) {
            JSONObject data = new JSONObject();
            try {
                data.put("user_id", my_user_id);
                data.put("comment", new_comment);
                msocket.emit("update_comment", data);
                user_comment.setText(new_comment);
                friend_db_manager.ChangeComment(my_user_id,new_comment);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
        }
    }

    void Connect() {
        try {
            //msocket = IO.socket("http://10.0.2.2:3000/");//안드로이드 avd사용시 로컬 호스트는 이 주소사용
            msocket = IO.socket("http://34.22.99.247:3000/");
            msocket.connect();
            msocket.on("search_id", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject data = (JSONObject) args[0];
                                user_data = data.getString("user_data");
                                Log.d("search_id event",user_data);
                                SetProfile(user_data);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
            msocket.on("server_check", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject data = (JSONObject) args[0];
                                String server_check = data.getString("server_check");
                                if(server_check.equals("add_friend")){
                                    friend_db_manager.FriendUpdate(my_user_id, friend_user_id, friend_nickname,friend_comment, 1, friend_profile_img_url, friend_background_img_url, friend_last_profile_update);
                                    add_friend_layout.setVisibility(View.INVISIBLE);
                                    ignore_layout.setVisibility(View.INVISIBLE);
                                    etc_button.setVisibility(View.VISIBLE);
                                    favorite_button_white.setVisibility(View.VISIBLE);
                                    chatting_layout.setVisibility(View.VISIBLE);
                                    call_layout.setVisibility(View.VISIBLE);
                                    video_call_layout.setVisibility(View.VISIBLE);
                                }else if(server_check.equals("ignore_friend")){
                                    friend_db_manager.FriendUpdate(my_user_id, friend_user_id, friend_nickname, friend_comment, 3, friend_profile_img_url, friend_background_img_url, friend_last_profile_update);
                                    add_friend_layout.setVisibility(View.INVISIBLE);
                                    ignore_layout.setVisibility(View.INVISIBLE);
                                    unignore_layout.setVisibility(View.VISIBLE);
                                    chatting_layout.setVisibility(View.INVISIBLE);
                                    call_layout.setVisibility(View.INVISIBLE);
                                    video_call_layout.setVisibility(View.INVISIBLE);
                                    etc_button.setVisibility(View.INVISIBLE);
                                    favorite_button_white.setVisibility(View.INVISIBLE);
                                    favorite_button_yellow.setVisibility(View.INVISIBLE);
                                }else if(server_check.equals("ignore_cancel")){
                                    friend_db_manager.IgnoreDelete(my_user_id, friend_user_id);
                                    add_friend_layout.setVisibility(View.VISIBLE);
                                    ignore_layout.setVisibility(View.VISIBLE);
                                    unignore_layout.setVisibility(View.INVISIBLE);
                                }else if(server_check.equals("delete_friend")){
                                    friend_db_manager.FriendUpdate(my_user_id, friend_user_id, friend_nickname, friend_comment, 0, friend_profile_img_url, friend_background_img_url, friend_last_profile_update);
                                    etc_button.setVisibility(View.INVISIBLE);
                                    favorite_button_white.setVisibility(View.INVISIBLE);
                                    favorite_button_yellow.setVisibility(View.INVISIBLE);
                                    chatting_layout.setVisibility(View.INVISIBLE);
                                    call_layout.setVisibility(View.INVISIBLE);
                                    video_call_layout.setVisibility(View.INVISIBLE);
                                    add_friend_layout.setVisibility(View.VISIBLE);
                                    ignore_layout.setVisibility(View.VISIBLE);
                                }else if(server_check.equals("favorite_friend")) {
                                    friend_db_manager.FriendUpdate(my_user_id, friend_user_id, friend_nickname, friend_comment, 2, friend_profile_img_url, friend_background_img_url, friend_last_profile_update);
                                    favorite_button_white.setVisibility(View.INVISIBLE);
                                    favorite_button_yellow.setVisibility(View.VISIBLE);
                                }else if(server_check.equals("favorite_cancel")){
                                    friend_db_manager.FriendUpdate(my_user_id, friend_user_id, friend_nickname, friend_comment, 1, friend_profile_img_url, friend_background_img_url, friend_last_profile_update);
                                    favorite_button_white.setVisibility(View.VISIBLE);
                                    favorite_button_yellow.setVisibility(View.INVISIBLE);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

            });
            msocket.on("get_image_url", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject data = (JSONObject) args[0];
                                String img_url = data.getString("img_url");
                                Log.d("get_image_url event", img_url);
                                try {
                                    JSONObject friend_array = new JSONObject(img_url);

                                    String profile_img_url = friend_array.getString("profile_img_url");
                                    friend_profile_img_url = profile_img_url;
                                    new_profile_img = profile_img_url;
                                    String background_img_url = friend_array.getString("background_img_url");
                                    friend_background_img_url = background_img_url;
                                    new_background_img = background_img_url;
                                    friend_db_manager.ChangeProfileImg(my_user_id,profile_img_url);
                                    friend_db_manager.ChangeBackgroundImg(my_user_id,background_img_url);
                                }catch (Exception e) {
                                    e.printStackTrace();
                                }

                            } catch (Exception e) {
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

    void SetProfile(String user_data) {
        try {
            JSONArray jsonarray = new JSONArray(user_data);
            JSONObject friend_array = jsonarray.getJSONObject(0);
            friend_user_id = friend_array.getString("user_id");
            friend_nickname = friend_array.getString("nickname");
            user_nickname.setText(friend_nickname);
            friend_comment = friend_array.getString("comment");
            user_comment.setText(friend_comment);
            friend_profile_img_url = friend_array.getString("profile_img_url");
            friend_background_img_url = friend_array.getString("background_img_url");
            friend_last_profile_update = friend_array.getString("last_profile_update");

            if(Objects.equals(my_user_id, friend_user_id)){
                my_nickname=friend_nickname;
                new_nickname=my_nickname;
                new_comment=friend_comment;
                new_profile_img = friend_profile_img_url;
                new_background_img = friend_background_img_url;
            }

            if(!Objects.equals(friend_profile_img_url, "null")) {
                @SuppressLint("SdCardPath")
                String image_path = "/data/data/com.example.chat_east/cache/C_E_preview/preview_" + ExtractFileNameFromURL(friend_profile_img_url) + ".jpg";
                File image_file = new File(image_path);
                ImageView profile_img = findViewById(R.id.profile_img);

                if (!image_file.exists()) {
                    new save_image_task_profile(this, friend_profile_img_url, new save_image_task_profile.SaveImageCallback() {
                        @Override
                        public void Onimagesaved(String friend_profile_img_urls, String image_path) {
                            if (friend_profile_img_url == friend_profile_img_urls) {
                                Glide.with(profile_view.this)
                                        .load(image_path)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL) // 캐싱 전략 설정
                                        .into(profile_img);
                            }
                        }
                    }).execute(friend_profile_img_url, "preview_" + ExtractFileNameFromURL(friend_profile_img_url) + ".jpg");
                } else {
                    Glide.with(this)
                            .load(image_path)
                            .diskCacheStrategy(DiskCacheStrategy.ALL) // 캐싱 전략 설정
                            .into(profile_img);
                }
            }

            if(!Objects.equals(friend_background_img_url, "null")) {
                @SuppressLint("SdCardPath")
                String image_path = "/data/data/com.example.chat_east/cache/C_E_preview/preview_" + ExtractFileNameFromURL(friend_background_img_url) + ".jpg";
                File image_file = new File(image_path);
                ImageView android_layout = findViewById(R.id.android_layout);

                if (!image_file.exists()) {
                    new save_image_task_profile(this, friend_background_img_url, new save_image_task_profile.SaveImageCallback() {
                        @Override
                        public void Onimagesaved(String friend_background_img_urls, String image_path) {
                            if (friend_background_img_url == friend_background_img_urls) {
                                Glide.with(profile_view.this)
                                        .load(image_path)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL) // 캐싱 전략 설정
                                        .into(android_layout);
                            }
                        }
                    }).execute(friend_background_img_url, "preview_" + ExtractFileNameFromURL(friend_background_img_url) + ".jpg");
                } else {
                    Glide.with(this)
                            .load(image_path)
                            .diskCacheStrategy(DiskCacheStrategy.ALL) // 캐싱 전략 설정
                            .into(android_layout);
                }
            }
            FriendTypeCheck();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String ExtractFileNameFromURL(String url) {
        // URL에서 "images/" 뒤의 부분만 추출
        String[] parts = url.split("/images/");
        if (parts.length > 1) {
            // 부분을 추출하여 ".jpg"를 제외한 숫자만 얻기
            String filepart = parts[1];
            int dotindex = filepart.indexOf('.');
            if (dotindex != -1) {
                return filepart.substring(0, dotindex); // ".jpg"를 제외한 숫자만 추출
            }
        }
        return ""; // 추출 실패 시 빈 문자열 반환
    }

    public void Call(View view){
        Intent intent = new Intent(this, call.class);
        intent.putExtra("friend_user_id", friend_user_id);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    public void ViewBackgroundImg(View view){
        if(nickname_edit_icon.getVisibility() == View.INVISIBLE && !Objects.equals(friend_background_img_url, "null")) {
            Intent intent = new Intent(this, photo_view.class);
            intent.putExtra("img_url", friend_background_img_url);
            startActivity(intent);
        }
    }

    public void BackActivity(View view){
        if(comment_edit_icon.getVisibility() == View.VISIBLE) {
            EditBackActivity1(null);
        }
        else {
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    public void EditBackActivity1(View view) {
        comment_edit_icon.setVisibility(View. INVISIBLE);
        nickname_edit_icon.setVisibility(View.INVISIBLE);
        nickname_line.setVisibility(View.INVISIBLE);
        comment_line.setVisibility(View.INVISIBLE);
        profile_camera.setVisibility(View.INVISIBLE);
        profile_edit_text.setVisibility(View.INVISIBLE);
        edit_confirm.setVisibility(View.INVISIBLE);
        profile_edit_camera.setVisibility(View.INVISIBLE);

        self_chat_text.setVisibility(View.VISIBLE);
        edit_text.setVisibility(View.VISIBLE);
        self_chatting_icon.setVisibility(View.VISIBLE);
        edit_icon.setVisibility(View.VISIBLE);
        vector.setVisibility(View.VISIBLE);

        LinearLayout nickname_linear_layout = findViewById(R.id.nickname_linear);
        int originalPaddingBottom = nickname_linear_layout.getPaddingBottom() - 15;
        nickname_linear_layout.setPadding(
                nickname_linear_layout.getPaddingLeft(),
                nickname_linear_layout.getPaddingTop(),
                nickname_linear_layout.getPaddingRight(),
                originalPaddingBottom
        );
        edit_layout_count--;

        user_nickname.setText(my_nickname);
        user_comment.setText(friend_comment);

        if(!Objects.equals(friend_background_img_url,new_background_img)){
            if(!Objects.equals(friend_background_img_url, "null")) {
                @SuppressLint("SdCardPath")
                String image_path = "/data/data/com.example.chat_east/cache/C_E_preview/preview_" + ExtractFileNameFromURL(friend_background_img_url) + ".jpg";
                File image_file = new File(image_path);
                ImageView android_layout = findViewById(R.id.android_layout);

                if (!image_file.exists()) {
                    new save_image_task_profile(this, friend_background_img_url, new save_image_task_profile.SaveImageCallback() {
                        @Override
                        public void Onimagesaved(String friend_background_img_urls, String image_path) {
                            if (friend_background_img_url == friend_background_img_urls) {
                                Glide.with(profile_view.this)
                                        .load(image_path)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL) // 캐싱 전략 설정
                                        .into(android_layout);
                            }
                        }
                    }).execute(friend_background_img_url, "preview_" + ExtractFileNameFromURL(friend_background_img_url) + ".jpg");
                } else {
                    Glide.with(this)
                            .load(image_path)
                            .diskCacheStrategy(DiskCacheStrategy.ALL) // 캐싱 전략 설정
                            .into(android_layout);
                }
            }
        }

        if(!Objects.equals(friend_profile_img_url,new_profile_img)){
            if(!Objects.equals(friend_profile_img_url, "null")) {
                @SuppressLint("SdCardPath")
                String image_path = "/data/data/com.example.chat_east/cache/C_E_preview/preview_" + ExtractFileNameFromURL(friend_profile_img_url) + ".jpg";
                File image_file = new File(image_path);
                ImageView profile_img = findViewById(R.id.profile_img);

                if (!image_file.exists()) {
                    new save_image_task_profile(this, friend_profile_img_url, new save_image_task_profile.SaveImageCallback() {
                        @Override
                        public void Onimagesaved(String friend_profile_img_urls, String image_path) {
                            if (friend_profile_img_url == friend_profile_img_urls) {
                                Glide.with(profile_view.this)
                                        .load(image_path)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL) // 캐싱 전략 설정
                                        .into(profile_img);
                            }
                        }
                    }).execute(friend_profile_img_url, "preview_" + ExtractFileNameFromURL(friend_profile_img_url) + ".jpg");
                } else {
                    Glide.with(this)
                            .load(image_path)
                            .diskCacheStrategy(DiskCacheStrategy.ALL) // 캐싱 전략 설정
                            .into(profile_img);
                }
            }
        }
    }

    public void EditBackActivity2(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View current_focus = getCurrentFocus();
        if (current_focus != null) {
            imm.hideSoftInputFromWindow(current_focus.getWindowToken(), 0);
        }

        edit_confirm.setVisibility(View.VISIBLE);

        if (nickname_edit_layout != null) {
            nickname_edit_layout.setVisibility(View.INVISIBLE);
        }
        if (nickname_edit_back_button != null) {
            nickname_edit_back_button.setVisibility(View.INVISIBLE);
        }
        if(comment_edit_layout != null) {
            comment_edit_layout.setVisibility(View.INVISIBLE);
        }
        if(comment_edit_back_button != null) {
            comment_edit_back_button.setVisibility(View.INVISIBLE);
        }
    }

    public void SearchUser(){
        if (msocket != null) {
            JSONObject data = new JSONObject();
            try {
                data.put("user_id",friend_user_id);
                msocket.emit("search_id", data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void FriendChatting(View view){
        if (msocket != null) {
            JSONObject data = new JSONObject();
            Object tag = view.getTag();

            int tag_num = 0;
            if (tag instanceof String) {
                String tag_str = (String) tag;
                try {
                    tag_num = Integer.parseInt(tag_str);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            else if (tag instanceof Integer) {
                tag_num = (Integer) tag;
            }

            try {
                data.put("user_id", my_user_id);
                data.put("people_count", tag_num);
                data.put("chatroom_name", "gdzzz");
                data.put("my_nickname", friend_db_manager.GetMyNickname(my_user_id));
                JSONArray user_id_list = new JSONArray();
                user_id_list.put(my_user_id);

                if(tag_num == 2) {
                    user_id_list.put(friend_user_id);
                    data.put("friend_nickname", friend_nickname);
                    Log.d("friend_nickname",friend_nickname);
                }
                data.put("user_id_list", user_id_list);

                int chatroom_type = tag_num;
                msocket.emit("create_chatroom", data);
                msocket.on("create_chatroom", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    JSONObject data = (JSONObject) args[0];
                                    String chatroom_id = data.getString("chatroom_id");
                                    String chatroom_name = data.getString("chatroom_name");
                                    Log.d("chatroom_type",String.valueOf(chatroom_type));

                                    chat_db_manager.AddChatroomList(my_user_id, Integer.parseInt(chatroom_id), chatroom_name, chatroom_type, 1, 1,"", "2020-01-01 00:00:00", 2, "", "2020-01-01 00:00:00", "2020-01-01 00:00:00");


                                    Intent intent = new Intent(profile_view.this, main_chatting.class);
                                    intent.putExtra(chatting_room.user_id_key, my_user_id);
                                    intent.putExtra(chatting_room.nickname_key, my_nickname);
                                    intent.putExtra("chatroom_id", Integer.parseInt(chatroom_id));

                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                    startActivity(intent);
                                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
        }
    }

    public void AddFriend(View view){
        if (msocket != null) {
            JSONObject data = new JSONObject();
            try {
                data.put("from_user_id", my_user_id);
                data.put("to_user_id",friend_user_id);
                msocket.emit("add_friend", data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void IgnoreFriend(View view){
        if (msocket != null) {
            JSONObject data = new JSONObject();
            try {
                data.put("from_user_id", my_user_id);
                data.put("to_user_id",friend_user_id);
                msocket.emit("ignore_friend", data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        etc_layout.setVisibility(View.INVISIBLE);
        etc_layout_open=false;
    }

    public void EtcMenu(View view){
        if(!etc_layout_open){
            etc_layout.setVisibility(View.VISIBLE);
            etc_layout_open=true;
        }else{
            etc_layout.setVisibility(View.INVISIBLE);
            etc_layout_open=false;
        }
    }

    public void FavoriteFriend(View view){
        if (msocket != null) {
            JSONObject data = new JSONObject();
            try {
                data.put("from_user_id", my_user_id);
                data.put("to_user_id",friend_user_id);
                msocket.emit("favorite_friend", data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void FavoriteCancel(View view){
        if (msocket != null) {
            JSONObject data = new JSONObject();
            try {
                data.put("from_user_id", my_user_id);
                data.put("to_user_id",friend_user_id);
                msocket.emit("favorite_cancel", data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void DeleteFriend(View view){
        if (msocket != null) {
            JSONObject data = new JSONObject();
            try {
                data.put("from_user_id", my_user_id);
                data.put("to_user_id",friend_user_id);
                msocket.emit("delete_friend", data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        etc_layout.setVisibility(View.INVISIBLE);
        etc_layout_open=false;
    }

    public void IgnoreCancel(View view){
        if (msocket != null) {
            JSONObject data = new JSONObject();
            try {
                data.put("from_user_id", my_user_id);
                data.put("to_user_id",friend_user_id);
                msocket.emit("ignore_cancel", data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void FriendTypeCheck(){
        String friend_type = friend_db_manager.FriendTypeCheck(my_user_id,friend_user_id);
        if(Objects.equals(friend_type, "-1")){//본인의 프로필이면
            self_chatting_layout.setVisibility(View.VISIBLE);
            edit_layout.setVisibility(View.VISIBLE);
        }else if(Objects.equals(friend_type, "0")){//아무런 관계도 없는 프로필이면
            add_friend_layout.setVisibility(View.VISIBLE);
            ignore_layout.setVisibility(View.VISIBLE);
        }else if(Objects.equals(friend_type, "1")){//친구면
            etc_button.setVisibility(View.VISIBLE);
            favorite_button_white.setVisibility(View.VISIBLE);
            chatting_layout.setVisibility(View.VISIBLE);
            call_layout.setVisibility(View.VISIBLE);
            video_call_layout.setVisibility(View.VISIBLE);
        }else if(Objects.equals(friend_type, "2")){//즐찾 친구면
            etc_button.setVisibility(View.VISIBLE);
            favorite_button_yellow.setVisibility(View.VISIBLE);
            chatting_layout.setVisibility(View.VISIBLE);
            call_layout.setVisibility(View.VISIBLE);
            video_call_layout.setVisibility(View.VISIBLE);
        }else if(Objects.equals(friend_type, "3")){//차단 친구면
            unignore_layout.setVisibility(View.VISIBLE);
        }
    }

    ////////////////////////////////////////////////////////////////////////////image upload
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int STORAGE_PERMISSION_CODE = 2;

    public void GalleryClick(View view){
        if(nickname_edit_icon.getVisibility() == View.INVISIBLE ) {
            if(!Objects.equals(friend_profile_img_url, "null")){
            Intent intent = new Intent(this, photo_view.class);
            intent.putExtra("img_url", friend_profile_img_url);
            startActivity(intent);}
            return;
        }

        image_upload_view_id = view.getId();

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
        Connect();
        friend_db_manager = new friend_db_manager(this);
        chat_db_manager = new chat_db_manager(this);
        if (request_code == PICK_IMAGE_REQUEST && result_code == RESULT_OK && data != null && data.getData() != null) {
            Uri image_uri = data.getData();
            String a = image_upload_view_id == R.id.profile_camera ? "3" : "2";
            if(a.equals("2")){//프로필 이미지
                current_profile_image_uri = image_uri;
                new_profile_img = String.valueOf(image_uri);
            }else if(a.equals("3")){
                current_background_image_uri = image_uri;
                new_background_img = String.valueOf(image_uri);
            }
            SetImage(image_uri,a);
        }
    }

    private void SetImage(Uri image_uri,String a) {
        try {
            ImageView imgset_view =findViewById(R.id.profile_img);
            if(Objects.equals(a, "3"))imgset_view = findViewById(R.id.android_layout);
            Glide.with(this)
                    .load(image_uri)  // 로컬 파일 URI를 사용하여 이미지 로드
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imgset_view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void EditConfirm(View view){
        if(current_profile_image_uri!=null){
            UploadImage(current_profile_image_uri,"2");
        }
        if(current_background_image_uri!=null){
            UploadImage(current_background_image_uri,"3");
        }

        comment_edit_icon.setVisibility(View. INVISIBLE);
        nickname_edit_icon.setVisibility(View.INVISIBLE);
        nickname_line.setVisibility(View.INVISIBLE);
        comment_line.setVisibility(View.INVISIBLE);
        profile_camera.setVisibility(View.INVISIBLE);
        profile_edit_text.setVisibility(View.INVISIBLE);
        edit_confirm.setVisibility(View.INVISIBLE);
        profile_edit_camera.setVisibility(View.INVISIBLE);
        self_chat_text.setVisibility(View.VISIBLE);
        edit_text.setVisibility(View.VISIBLE);
        self_chatting_icon.setVisibility(View.VISIBLE);
        edit_icon.setVisibility(View.VISIBLE);
        vector.setVisibility(View.VISIBLE);

        LinearLayout nickname_linear_layout = findViewById(R.id.nickname_linear);
        int originalPaddingBottom = nickname_linear_layout.getPaddingBottom() - 15;
        nickname_linear_layout.setPadding(
                nickname_linear_layout.getPaddingLeft(),
                nickname_linear_layout.getPaddingTop(),
                nickname_linear_layout.getPaddingRight(),
                originalPaddingBottom
        );
        edit_layout_count--;

        if(!Objects.equals(my_nickname, new_nickname)){
            UpdateNickname();
        }
        if(!Objects.equals(friend_comment, new_comment)){
            UpdateComment();
        }


    }

    public void GetImageUrl(){
        if (msocket != null) {
            JSONObject data = new JSONObject();
            try {
                data.put("user_id", my_user_id);
                msocket.emit("get_image_url", data);
            } catch (Exception e) {
            }
        }
    }

    private void UploadImage(Uri image_uri,String num) {
        try {
            String file_path = GetRealPathFromURI(image_uri);
            File file = new File(file_path);

            RequestBody request_file = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), request_file);

            RequestBody user_id = RequestBody.create(MediaType.parse("multipart/form-data"), my_user_id);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://34.22.99.247:3000/")
                    .client(new OkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            retrofit2_api_profile service = retrofit.create(retrofit2_api_profile.class);
            RequestBody path_type = RequestBody.create(MediaType.parse("multipart/form-data"),num);

            Call<ResponseBody> call = service.uploadFile(body, user_id, path_type);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        GetImageUrl();
                    } else {
                    }
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String GetRealPathFromURI(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else {
            return uri.getPath();
        }
    }

    @Override
    public void onRequestPermissionsResult(int request_code, @NonNull String[] permissions, @NonNull int[] grant_results) {
        super.onRequestPermissionsResult(request_code, permissions, grant_results);
        if (request_code == STORAGE_PERMISSION_CODE) {
            if (grant_results.length > 0 && grant_results[0] == PackageManager.PERMISSION_GRANTED) {
                OpenGallery();
            } else {
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////image upload

    @Override
    protected void onStop() {//액티비티 종료시 실행
        super.onStop();
        if (msocket != null) {
            msocket.disconnect();//소켓을 닫는다
        }
        friend_db_manager.close();
    }
    @Override
    protected void onDestroy() {//어플리케이션 종료시 실행
        super.onDestroy();
        if (msocket != null) {
            msocket.disconnect(); // 소켓을 닫는다
        }
        friend_db_manager.close();
    }

    @Override
    protected void onResume() {//액티비티 종료시 실행
        super.onResume();
        Connect();
        friend_db_manager = new friend_db_manager(this);
        chat_db_manager = new chat_db_manager(this);
    }
}