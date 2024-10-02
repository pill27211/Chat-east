package com.example.chat_east;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

//pill
import android.provider.MediaStore;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import androidx.annotation.NonNull;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.File;
import okhttp3.MultipartBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import com.example.chat_east.api.retrofit2_api_chatroom;



public class create_chatroom extends AppCompatActivity {
    private Socket msocket;
    private String my_user_id, my_nickname;
    private TextView confirm_text, confirm_text2, chatroom_name_count;
    private friend_db_manager friend_db_manager;
    private chat_db_manager chat_db_manager;
    private int friend_count=0;
    private JSONArray selected_friends = new JSONArray();
    private EditText chatroom_name;
    private Uri selected_image_uri;


    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        setContentView(R.layout.create_chatroom);
        friend_db_manager = new friend_db_manager(this);
        chat_db_manager = new chat_db_manager(this);

        confirm_text = findViewById(R.id.confirm_text);
        confirm_text2 = findViewById(R.id.confirm_text2);
        chatroom_name = findViewById(R.id.chatroom_name);
        chatroom_name_count = findViewById(R.id.chatroom_name_count);

        my_nickname = message_receive.nickname;
        my_user_id = message_receive.user_id;

        selected_friends.put(my_user_id);
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("select_friends")) {
            ArrayList<String> friends_list = intent.getStringArrayListExtra("select_friends");
            if (friends_list != null) {
                for (String friend : friends_list) {
                    selected_friends.put(friend);
                    friend_count++;
                    Log.d("friend_list",friend);
                }
            }
        }

        chatroom_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {    }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int text_length = s.length();
                chatroom_name_count.setText(text_length + "/20");
                if(text_length>1){
                    confirm_text.setVisibility(View.GONE);
                    confirm_text2.setVisibility(View.VISIBLE);
                }else{
                    confirm_text.setVisibility(View.VISIBLE);
                    confirm_text2.setVisibility(View.GONE);
                }

                if (text_length > 20) {
                    chatroom_name.setText(s.subSequence(0, 20));
                    chatroom_name.setSelection(20); // 커서를 마지막으로 이동
                }
            }
            @Override
            public void afterTextChanged(Editable s){   }
        });
    }

    public void MadeChatroom(View view) {
        Log.d("asdasd","madechjatroom");
        try {
            msocket = IO.socket("http://34.22.99.247:3000/");
            msocket.connect();
            JSONObject data = new JSONObject();
            data.put("people_count", friend_count+2);
            data.put("user_id_list", selected_friends);
            data.put("chatroom_name",chatroom_name.getText().toString());
            msocket.emit("create_chatroom", data);
            msocket.on("create_chatroom", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject data = (JSONObject) args[0];
                                int new_chatroom_id = data.getInt("chatroom_id");

                                //pill
                                if (selected_image_uri != null) {
                                    UploadImage(selected_image_uri, new_chatroom_id);
                                }
                                //pill

                                chat_db_manager.AddChatroomList(my_user_id, new_chatroom_id, chatroom_name.getText().toString(), 3, 1, 1, "", "2020-01-01 00:00:00", friend_count+1, "", "null", "2020-01-01 00:00:00");
                                Intent intent = new Intent(create_chatroom.this, main_chatting.class);
                                intent.putExtra(chatting_room.user_id_key, my_user_id);
                                intent.putExtra("chatroom_id", new_chatroom_id);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                msocket.disconnect();
                                startActivity(intent);
                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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

    public void BackPress(View view){
        finish();
    }
    public void ImageUpload(View view) {
        OpenGallery();
    }

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int STORAGE_PERMISSION_CODE = 2;

    public void GalleryClick(View view){
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
        if (request_code == PICK_IMAGE_REQUEST && result_code == RESULT_OK && data != null && data.getData() != null) {
            selected_image_uri = data.getData();
            SetImage(selected_image_uri);
        }
    }
    private void SetImage(Uri image_uri) {
        try {
            ImageView profile_img =findViewById(R.id.profile_img);
            Glide.with(this)
                    .load(image_uri)  // 로컬 파일 URI를 사용하여 이미지 로드
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(profile_img);
        } catch (Exception e) {
            e.printStackTrace();
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


    private void UploadImage(Uri image_uri, int chatroom_id) {
        try {
            String file_path = GetRealPathfromURI(image_uri);
            File file = new File(file_path);

            RequestBody request_file = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), request_file);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://34.22.99.247:3000/")
                    .client(new OkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            retrofit2_api_chatroom service = retrofit.create(retrofit2_api_chatroom.class);
            RequestBody request_chatroom_id = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(chatroom_id));
            RequestBody path_type = RequestBody.create(MediaType.parse("multipart/form-data"),"4");

            Call<ResponseBody> call = service.uploadFile(body, request_chatroom_id, path_type);
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

    private String GetRealPathfromURI(Uri uri) {
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
}


