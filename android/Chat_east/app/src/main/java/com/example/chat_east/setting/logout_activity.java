package com.example.chat_east.setting;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chat_east.R;
import com.example.chat_east.login_activity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.example.chat_east.message_receive;

import org.json.JSONObject;

import java.util.Objects;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class logout_activity extends AppCompatActivity {

    private GoogleSignInClient google_signin_client;
    private FirebaseAuth m_auth;
    private Button btn_logout_confirm;
    private Button btn_logout_cancel;
    private Socket msocket;


    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        setContentView(R.layout.activity_logout);

        m_auth = FirebaseAuth.getInstance();
        google_signin_client = GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build());

        btn_logout_confirm = findViewById(R.id.btn_logout_confirm);
        btn_logout_cancel = findViewById(R.id.btn_logout_cancel);

        btn_logout_confirm.setOnClickListener(view -> Signout());
        btn_logout_cancel.setOnClickListener(view -> finish());
    }

    private void Signout() {
        google_signin_client.signOut()
                .addOnCompleteListener(this, task -> {
                    m_auth.signOut();
                    Logout();

                    Intent service_intent = new Intent(this, message_receive.class);
                    stopService(service_intent);

                    Toast.makeText(logout_activity.this, R.string.success_logout, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, login_activity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
    }

    void Logout() {
        try {
            //msocket = IO.socket("http://10.0.2.2:3000/");//안드로이드 avd사용시 로컬 호스트는 이 주소사용
            msocket = IO.socket("http://34.22.99.247:3000/");
            msocket.connect();
            JSONObject data = new JSONObject();
            data.put("user_id", message_receive.user_id);
            msocket.emit("logout", data);
        } catch (Exception e) {
            Log.e("SOCKET", "Connection error", e);
            e.printStackTrace();
        }
    }
}