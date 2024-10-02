package com.example.chat_east;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseAuth;


public class login_activity extends AppCompatActivity {
    private static final int rc_sign_in = 9001;
    private GoogleSignInClient google_signin_client;
    private GoogleSignInAccount gsa;
    private FirebaseAuth m_auth;
    private SignInButton btn_google_login;
    private Button btn_google_logout;

    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        setContentView(R.layout.login_display);
        m_auth = FirebaseAuth.getInstance();

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        google_signin_client = GoogleSignIn.getClient(this, googleSignInOptions);
        gsa = GoogleSignIn.getLastSignedInAccount(login_activity.this);
        btn_google_login = findViewById(R.id.btn_google_sign_in);
        btn_google_login.setOnClickListener(view -> {
            if (gsa != null) // 로그인 되있는 경우
                Toast.makeText(login_activity.this, R.string.status_login, Toast.LENGTH_SHORT).show();
            else
                Signin();
        });

        btn_google_logout = findViewById(R.id.btn_logout_google);
        btn_google_logout.setOnClickListener(view -> {
            SignOut(); //로그아웃
        });
    }
    private void Signin(){
        Intent sign_in_intent = google_signin_client.getSignInIntent();
        startActivityForResult(sign_in_intent, rc_sign_in);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == rc_sign_in) {
            Intent intent = new Intent(getApplicationContext(), login_check.class);
            startActivity(intent);
            finish();//현재 액티비티 종료
        }
    }

    private void SignOut() {//로그아웃
        google_signin_client.signOut()
                .addOnCompleteListener(this, task -> {
                    m_auth.signOut();
                    Toast.makeText(login_activity.this, R.string.success_logout, Toast.LENGTH_SHORT).show();
                });
        gsa = null;
    }

}