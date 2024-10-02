package com.example.chat_east;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import org.webrtc.DataChannel;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

public class call extends AppCompatActivity {
    chat_db_manager chat_db_manager;
    friend_db_manager friend_db_manager;
    private Socket msocket;
    ConstraintLayout from_me_container, from_friend_container, call_connect_container;
    String my_user_id, my_nickname, friend_user_id, friend_profile_img_url, friend_nickname, my_profile_img_url;
    ImageView connect_my_image, connect_friend_image;
    TextView connect_my_nickname, connect_friend_nickname;
    int call_type, answer_type=0;
    CardView me_mic_off, me_mic_on, me_call_disconnect, me_speaker_off, me_speaker_on, friend_mic_off, friend_mic_on, connect_mic_off, connect_mic_on, connect_call_disconnect, connect_speaker_off, connect_speaker_on;
    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        setContentView(R.layout.call);

        chat_db_manager = new chat_db_manager(this);
        friend_db_manager = new friend_db_manager(this);

        my_user_id = message_receive.user_id;
        my_nickname = message_receive.nickname;

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("friend_user_id")) {
            friend_user_id = intent.getStringExtra("friend_user_id");
            Log.d("friend_user_id",String.valueOf(friend_user_id));
        }
        if (intent != null && intent.hasExtra("call_type")) {
            call_type = intent.getIntExtra("call_type",0);//[내가검,상대가검
            Log.d("call_type",String.valueOf(call_type));
        }

        from_me_container = findViewById(R.id.from_me_container);
        from_friend_container = findViewById(R.id.from_friend_container);
        call_connect_container = findViewById(R.id.call_connect_container);

        me_mic_off = findViewById(R.id.me_mic_off);
        me_mic_on = findViewById(R.id.me_mic_on);
        me_call_disconnect = findViewById(R.id.me_call_disconnect);
        me_speaker_off = findViewById(R.id.me_speaker_off);
        me_speaker_on = findViewById(R.id.me_speaker_on);
        friend_mic_off = findViewById(R.id.friend_mic_off);
        friend_mic_on = findViewById(R.id.friend_mic_on);
        connect_mic_off = findViewById(R.id.connect_mic_off);
        connect_mic_on = findViewById(R.id.connect_mic_on);
        connect_call_disconnect = findViewById(R.id.connect_call_disconnect);
        connect_speaker_off = findViewById(R.id.connect_speaker_off);
        connect_speaker_on = findViewById(R.id.connect_speaker_on);

        connect_my_image = findViewById(R.id.connect_my_image);
        connect_friend_image = findViewById(R.id.connect_friend_image);

        connect_my_nickname = findViewById(R.id.connect_my_nickname);
        connect_friend_nickname = findViewById(R.id.connect_friend_nickname);

        if(friend_db_manager.IsFriend(my_user_id,friend_user_id)){
            friend_profile_img_url = friend_db_manager.GetProfileImg(my_user_id,friend_user_id);
            Log.d("friend_user_id",String.valueOf(friend_user_id));
            Log.d("friend_profile_img_url",friend_profile_img_url);
            friend_nickname = friend_db_manager.GetNickname(my_user_id,friend_user_id);
            Log.d("friend_nick",friend_nickname);
        }else{
            friend_profile_img_url = chat_db_manager.GetProfileImg(friend_user_id);
            Log.d("friend_user_id",String.valueOf(friend_user_id));
            Log.d("friend_profile_img_url",friend_profile_img_url);
            friend_nickname = chat_db_manager.GetNickname(my_user_id,friend_user_id);
            Log.d("friend_nick",friend_nickname);
        }

        my_profile_img_url =friend_db_manager.GetMyProfileUrl(my_user_id);

        Connect();
        if(call_type==1){
            EnterCall();
        }
        SetCallAnimation();
        from_me_container.setVisibility(View.GONE);
        from_friend_container.setVisibility(View.GONE);
        call_connect_container.setVisibility(View.GONE);

        if(!Objects.equals(my_profile_img_url, "null")) {
            @SuppressLint("SdCardPath")
            String image_path = "/data/data/com.example.chat_east/cache/C_E_preview/preview_" + ExtractFileNameFromURL(my_profile_img_url) + ".jpg";
            Glide.with(this)
                    .load(image_path)
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // 캐싱 전략 설정
                    .into(connect_my_image);
        }

        connect_my_nickname.setText(my_nickname);

        if(!Objects.equals(friend_profile_img_url, "null")) {
            @SuppressLint("SdCardPath")
            String image_path = "/data/data/com.example.chat_east/cache/C_E_preview/preview_" + ExtractFileNameFromURL(friend_profile_img_url) + ".jpg";
            Glide.with(this)
                    .load(image_path)
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // 캐싱 전략 설정
                    .into(connect_friend_image);
        }
        connect_friend_nickname.setText(friend_nickname);
        TextView me_friend_nickname = findViewById(R.id.me_friend_nickname);
        me_friend_nickname.setText(friend_nickname);
        TextView friend_friend_nickname = findViewById(R.id.friend_friend_nickname);
        friend_friend_nickname.setText(friend_nickname);

        if(call_type==0) {
            from_me_container.setVisibility(View.VISIBLE);
            if(!Objects.equals(friend_profile_img_url, "null")) {
                @SuppressLint("SdCardPath")
                String image_path = "/data/data/com.example.chat_east/cache/C_E_preview/preview_" + ExtractFileNameFromURL(friend_profile_img_url) + ".jpg";
                ImageView me_friend_image = findViewById(R.id.me_friend_image);
                Glide.with(this)
                        .load(image_path)
                        .diskCacheStrategy(DiskCacheStrategy.ALL) // 캐싱 전략 설정
                        .into(me_friend_image);
            }
            CallRequest();
        }else if(call_type==1){
            from_friend_container.setVisibility(View.VISIBLE);
            if(!Objects.equals(friend_profile_img_url, "null")) {
                @SuppressLint("SdCardPath")
                String image_path = "/data/data/com.example.chat_east/cache/C_E_preview/preview_" + ExtractFileNameFromURL(friend_profile_img_url) + ".jpg";
                ImageView friend_friend_image = findViewById(R.id.friend_friend_image);
                Glide.with(this)
                        .load(image_path)
                        .diskCacheStrategy(DiskCacheStrategy.ALL) // 캐싱 전략 설정
                        .into(friend_friend_image);
            }

        }
    }

    void Connect() {
        try {
            //msocket = IO.socket("http://10.0.2.2:3000/");//안드로이드 avd사용시 로컬 호스트는 이 주소사용
            msocket = IO.socket("http://34.22.99.247:3000/");
            msocket.connect();//위 주소로 연결

            msocket.on("voice_talk_answer", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject data = (JSONObject) args[0];
                                int answer_type = data.getInt("answer_type");
                                Log.d("voice_talk_answer event", String.valueOf(answer_type));
                                if(answer_type == 0) { // 상대방이 거절한 경우
                                    if(call_type == 0){ // 내가 건 사람일 때
                                        Toast.makeText(getApplicationContext(), "상대방이 통화를 거절하였습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                    Log.d("CallActivity", "anser_type==0");
                                    finish();
                                }else if(answer_type == 1){ // 상대방이 수락한 경우
                                    WebRTCManager(call.this); // WebRTC 초기화
                                    CreatePeerConnection(getIceServers()); // ICE 서버 설정
                                    AddLocalMediaTracks(); // 로컬 미디어 트랙 추가
                                    if(call_type == 0){ // 내가 건 사람일 때
                                        CreateOffer(); // Offer 생성
                                    }
                                }else if(answer_type == -1) { //연결이 종료된경우
                                    Log.d("CallActivity", "anser_type==-1");
                                    Toast.makeText(getApplicationContext(), "통화가 종료되었습니다.", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });

            msocket.on("sdp_offer", new Emitter.Listener() { // 송신자에게서 받은 SDP offer
                @Override
                public void call(Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject data = (JSONObject) args[0];
                                String sdp = data.getString("sdp");
                                Log.d("offer_sdp",sdp);
                                SessionDescription remoteOffer = new SessionDescription(SessionDescription.Type.OFFER, sdp);
                                CreateAnswer(remoteOffer);
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });

            msocket.on("sdp_answer", new Emitter.Listener() { // 수신자에게서 받은 SDP answer
                @Override
                public void call(Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject data = (JSONObject) args[0];
                                String sdp = data.getString("sdp");
                                Log.d("answer_sdp",sdp);
                                SessionDescription remoteAnswer = new SessionDescription(SessionDescription.Type.ANSWER, sdp);
                                peerConnection.setRemoteDescription(new SdpObserver() {
                                    @Override
                                    public void onCreateSuccess(SessionDescription sessionDescription) {}
                                    @Override
                                    public void onSetSuccess() {}
                                    @Override
                                    public void onCreateFailure(String s) {}
                                    @Override
                                    public void onSetFailure(String s) {}
                                }, remoteAnswer);

                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });

            msocket.on("ice_candidate", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    runOnUiThread(() -> {
                        try {
                            JSONObject data = (JSONObject) args[0];
                            String sdpMid = data.getString("sdp_mid");
                            int sdpMLineIndex = data.getInt("sdp_m_line_index");
                            String sdp = data.getString("sdp");
                            IceCandidate iceCandidate = new IceCandidate(sdpMid, sdpMLineIndex, sdp);
                            peerConnection.addIceCandidate(iceCandidate);
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            e.printStackTrace();
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

    private List<PeerConnection.IceServer> getIceServers() {
        return List.of(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));
    }

    void CallRequest() {//메세지 전송 함수
        Log.d("callrequest","call request");
        if (msocket != null) {//연결된경우에만 보내기 가능
            JSONObject data = new JSONObject();
            try {
                data.put("my_user_id", my_user_id);
                Log.d("user_id",my_user_id);
                data.put("my_nickname", my_nickname);
                data.put("friend_user_id", friend_user_id);
                Log.d("friend_id",friend_user_id);
                data.put("friend_nickname",friend_nickname);
                msocket.emit("voice_talk_offer", data);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    void CallAccept() {//메세지 전송 함수
        Log.d("callaccept","call accept");
        if (msocket != null) {//연결된경우에만 보내기 가능
            JSONObject data = new JSONObject();
            try {
                data.put("my_user_id", my_user_id);
                data.put("friend_user_id", friend_user_id);
                data.put("answer_type",1);
                msocket.emit("voice_talk_answer", data);
            } catch (Exception e) {

                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    void EnterCall() {//메세지 전송 함수
        if (msocket != null) {//연결된경우에만 보내기 가능
            JSONObject data = new JSONObject();
            try {
                data.put("my_user_id", my_user_id);
                data.put("friend_user_id", friend_user_id);
                msocket.emit("enter_call", data);
            } catch (Exception e) {

                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    void CallDeny(){
        if (msocket != null) {
            JSONObject data = new JSONObject();
            try {
                data.put("my_user_id", my_user_id);
                data.put("friend_user_id", friend_user_id);
                //data.put("answer_type",answer_type);
                data.put("answer_type",0);
                msocket.emit("voice_talk_answer", data);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    void CallDisconnect(){
        if (msocket != null) {
            JSONObject data = new JSONObject();
            try {
                data.put("my_user_id", my_user_id);
                data.put("friend_user_id", friend_user_id);
                data.put("answer_type",-1);
                msocket.emit("voice_talk_answer", data);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    private void SendOfferToRemote(SessionDescription sessionDescription) {
        if (msocket != null) {
            JSONObject data = new JSONObject();
            try {
                data.put("my_user_id", my_user_id);
                data.put("friend_user_id", friend_user_id);
                data.put("sdp", sessionDescription.description);
                msocket.emit("sdp_offer", data);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    private void SendAnswerToRemote(SessionDescription sessionDescription) {
        if (msocket != null) {
            JSONObject data = new JSONObject();
            try {
                data.put("my_user_id", my_user_id);
                data.put("friend_user_id", friend_user_id);
                data.put("sdp", sessionDescription.description);
                msocket.emit("sdp_answer", data);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }


    private void SendIceCandidateToRemote(IceCandidate iceCandidate) {
        if (msocket != null) {
            JSONObject data = new JSONObject();
            try {
                data.put("my_user_id", my_user_id);
                data.put("friend_user_id", friend_user_id);
                data.put("sdp_mid", iceCandidate.sdpMid);  // ICE 후보의 sdpMid
                data.put("sdp_m_line_index", iceCandidate.sdpMLineIndex);  // ICE 후보의 sdpMLineIndex
                data.put("sdp", iceCandidate.sdp);  // ICE 후보의 SDP
                msocket.emit("ice_candidate", data);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }



    void SetCallAnimation(){
        ImageView calling_ani;
        if(call_type == 0){
            calling_ani = findViewById(R.id.me_calling_ani);
        }else{
            calling_ani = findViewById(R.id.friend_calling_ani);
        }
        ObjectAnimator colorAnimator = ObjectAnimator.ofArgb(
                calling_ani,
                "backgroundColor",
                Color.RED,
                Color.parseColor("#FFA500"),
                Color.parseColor("#FFA500"),
                Color.YELLOW,
                Color.YELLOW,
                Color.GREEN,
                Color.GREEN,
                Color.CYAN,
                Color.CYAN,
                Color.BLUE,
                Color.BLUE,
                Color.MAGENTA,
                Color.MAGENTA,
                Color.RED
        );

        colorAnimator.setDuration(4500);
        colorAnimator.setRepeatCount(ValueAnimator.INFINITE);
        colorAnimator.setRepeatMode(ValueAnimator.RESTART);
        colorAnimator.start();
    }

    private PeerConnectionFactory peerConnectionFactory;
    private PeerConnection peerConnection;

    public void WebRTCManager(Context context) {
        // 1. WebRTC 초기화
        PeerConnectionFactory.InitializationOptions initializationOptions =
                PeerConnectionFactory.InitializationOptions.builder(context)
                        .createInitializationOptions();
        PeerConnectionFactory.initialize(initializationOptions);

        // 2. PeerConnectionFactory 생성
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        peerConnectionFactory = PeerConnectionFactory.builder()
                .setOptions(options)
                .createPeerConnectionFactory();
    }

    public void CreatePeerConnection(List<PeerConnection.IceServer> iceServers) {
        // 3. PeerConnection 설정
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, new PeerConnection.Observer() {
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                SendIceCandidateToRemote(iceCandidate);
            }
            @Override
            public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {}
            @Override
            public void onAddStream(MediaStream mediaStream) {
                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), "전화가 연결되었습니다.", Toast.LENGTH_SHORT).show();
                    from_me_container.setVisibility(View.GONE);
                    from_friend_container.setVisibility(View.GONE);
                    call_connect_container.setVisibility(View.VISIBLE);
                    connect_mic_off.setVisibility(View.GONE);
                    connect_mic_on.setVisibility(View.VISIBLE);
                    connect_speaker_on.setVisibility(View.GONE);
                    connect_speaker_off.setVisibility(View.VISIBLE);
                });
            }
            @Override
            public void onRemoveStream(MediaStream mediaStream) {}
            @Override
            public void onSignalingChange(PeerConnection.SignalingState signalingState) {}
            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {}
            @Override
            public void onIceConnectionReceivingChange(boolean b) {}
            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {}
            @Override
            public void onDataChannel(DataChannel dataChannel) {}
            @Override
            public void onRenegotiationNeeded() {}
            @Override
            public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
                for (MediaStream mediaStream : mediaStreams) {
                    for (AudioTrack audioTrack : mediaStream.audioTracks) {
                        audioTrack.setEnabled(true);
                    }
                }
            }
        });
    }

    public void CreateOffer() {
        peerConnection.createOffer(new SdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                peerConnection.setLocalDescription(new SdpObserver() {
                    @Override
                    public void onCreateSuccess(SessionDescription sessionDescription) {}
                    @Override
                    public void onSetSuccess() {
                        SendOfferToRemote(sessionDescription);
                    }
                    @Override
                    public void onCreateFailure(String s) {}
                    @Override
                    public void onSetFailure(String s) {}
                }, sessionDescription);
            }
            @Override
            public void onSetSuccess() {}
            @Override
            public void onCreateFailure(String s) {}
            @Override
            public void onSetFailure(String s) {}
        }, new MediaConstraints());
    }

    public void CreateAnswer(SessionDescription remoteOffer) {
        peerConnection.setRemoteDescription(new SdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {}
            @Override
            public void onSetSuccess() {
                peerConnection.createAnswer(new SdpObserver() {
                    @Override
                    public void onCreateSuccess(SessionDescription sessionDescription) {
                        peerConnection.setLocalDescription(new SdpObserver() {
                            @Override
                            public void onCreateSuccess(SessionDescription sessionDescription) {}
                            @Override
                            public void onSetSuccess() {
                                SendAnswerToRemote(sessionDescription);
                            }
                            @Override
                            public void onCreateFailure(String s) {}
                            @Override
                            public void onSetFailure(String s) {}
                        }, sessionDescription);
                    }
                    @Override
                    public void onSetSuccess() {}
                    @Override
                    public void onCreateFailure(String s) {}
                    @Override
                    public void onSetFailure(String s) {}
                }, new MediaConstraints());
            }
            @Override
            public void onCreateFailure(String s) {}
            @Override
            public void onSetFailure(String s) {}
        }, remoteOffer);
    }

    public void AddLocalMediaTracks() {
        AudioSource audioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
        AudioTrack localAudioTrack = peerConnectionFactory.createAudioTrack("101", audioSource);
        MediaStream mediaStream = peerConnectionFactory.createLocalMediaStream("mediaStream");
        mediaStream.addTrack(localAudioTrack);
        peerConnection.addStream(mediaStream);
    }

    public void DenyCall(View view){
        CallDeny();
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    public void AcceptCall(View view){
        CallAccept();
    }

    public void MicOn(View view){
        me_mic_off.setVisibility(View.GONE);
        me_mic_on.setVisibility(View.VISIBLE);
        friend_mic_off.setVisibility(View.GONE);
        friend_mic_on.setVisibility(View.VISIBLE);
        connect_mic_off.setVisibility(View.GONE);
        connect_mic_on.setVisibility(View.VISIBLE);
    }

    public void MicOff(View view){
        me_mic_off.setVisibility(View.VISIBLE);
        me_mic_on.setVisibility(View.GONE);
        friend_mic_off.setVisibility(View.VISIBLE);
        friend_mic_on.setVisibility(View.GONE);
        connect_mic_off.setVisibility(View.VISIBLE);
        connect_mic_on.setVisibility(View.GONE);
    }

    public void CallDisconnect(View view){
        CallDisconnect();
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    public void SpeakerOn(View view){
        me_speaker_off.setVisibility(View.GONE);
        me_speaker_on.setVisibility(View.VISIBLE);
        connect_speaker_off.setVisibility(View.GONE);
        connect_speaker_on.setVisibility(View.VISIBLE);
    }

    public void SpeakerOff(View view){
        connect_speaker_off.setVisibility(View.VISIBLE);
        connect_speaker_on.setVisibility(View.GONE);
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

    @Override
    protected void onDestroy() {
        msocket.disconnect();
        super.onDestroy();
    }
}
