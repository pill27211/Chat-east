<div align="center">

# 💬 Chat-East 🗨️

</div>

> 🌱 **2024년, 졸업작품이던 온라인 저지(`hustoj` 오픈소스 기반)를 만든 뒤 이어서 도전한 프로젝트입니다.** 그때까지 개발 경험이라곤 그 온라인 저지를 오픈소스 뜯어가며 세워본 게 전부라, 서버를 밑바닥부터 구축하는 것도, 실시간 통신도, 팀 협업 워크플로우도 대부분이 처음이자 맨땅에 헤딩이었습니다. Socket.IO를 바닥부터 파보며 부딪힌 결과물이라, 지금 다시 짜면 많이 다르겠지만 그 시절 모습 그대로 남겨둡니다.
>
> <sub>_(2026 정리 메모: 당시엔 `node_modules`를 통째로 커밋하고 `.env`·에러로그까지 올리는 등 git 위생을 잘 몰랐습니다. `.env`에 실제 민감한 값이 있었던 건 아니고(로컬 개발용 포트·DB 설정뿐이며, Firebase 비공개 키는 애초에 커밋하지 않았습니다), 그래도 원칙적으로 버전 관리에 들어가면 안 되는 것들이라 코드는 손대지 않고 히스토리에서 의존성·설정·로그만 걷어낸 뒤 `.gitignore`와 `.env.example`을 추가했습니다.)_</sub>

# Chat-East (Easy Fast Chatting Application)
> **협성대학교 소프트웨어학과 팀 프로젝트** <br/> **개발기간 : 2024-07 ~ 2024-09**

## 깃 주소

> **프론트 엔드** : [https://github.com/Chat-east/android](https://github.com/pill27211/Chat-east/tree/master/android)<br>
> **백 엔드** : [https://github.com/Chat-east/backend](https://github.com/pill27211/Chat-east/tree/master/backend)<br>

## 개발팀 소개

|      이진수       |          정필선         |                                                                                      
| :------------------------------------------------------------------------------: | :---------------------------------------------------------------------------------------------------------------------------------------------------: |
|   <a href="https://github.com/kiwijuse" target="_blank"><img width="160px" src="https://avatars.githubusercontent.com/u/151356073?v=4" /></a>   |                      <a href="https://github.com/pill27211" target="_blank"><img width="160px" src="https://avatars.githubusercontent.com/u/120912574?v=4" /></a>    |
|   [@kiwijuse](https://github.com/kiwijuse)   |    [@pill27211](https://github.com/pill27211)  |
| 협성대학교 소프트웨어학과 4학년 | 협성대학교 소프트웨어학과 4학년 |

## 프로젝트 개요

**&nbsp;&nbsp;학교 졸업작품으로 [AlgoWiki](https://algowiki.co.kr/)를 개발하면서, 남은 과제 중 하나로 채점 진행률 실시간 업데이트가 있었습니다. 이를 위해서 적절한 통신 기술이 필요했지만 당시 저희는 이쪽 지식이 전무한 상태였고, 이참에 통신 공부를 제대로 해보자 다짐하였습니다.<br>
이에 저희는 [Express](https://expressjs.com/ko/) 기반의 서버와 [Socket.IO](https://socket.io/)를 필두로 채팅 앱을 바닥부터 구현하기로 했으며, Retrofit2를 이용한 REST API 통신, [Firebase Cloud Messaging](https://firebase.google.com/docs/reference/fcm/rest?hl=ko), [WebRTC](https://webrtc.org/?hl=ko)와 같이 외부 API를 우리의 DB 구조, 목적에 맞게 활용하는 방법 또한 터득하였습니다.**

**&nbsp;&nbsp;Developing [AlgoWiki](https://algowiki.co.kr/) as a school graduation work, there was a real-time update of the scoring progress as one of the remaining tasks. We needed proper communication skills for this, but we didn't have any knowledge at the time, and we decided to take this opportunity to study communication properly.<br>
In response, we decided to implement chat apps from the bottom, starting with [Express-based](https://expressjs.com/ko/) servers and [Socket.IO](https://socket.io/), and we also learned how to use external APIs for our DB structure and purpose, such as REST API communication using Retrofit2, [Firebase Cloud Messaging](https://firebase.google.com/docs/reference/fcm/rest?hl=ko), and [WebRTC](https://webrtc.org/?hl=ko).**

## 시작 가이드
### Requirements
For building and running the application you need:
- [Node.js 22.9.0](https://nodejs.org/dist/v22.9.0/node-v22.9.0-x64.msi)
- [Android Studio Koala 2024.1.1.11](https://redirector.gvt1.com/edgedl/android/studio/install/2024.1.1.11/android-studio-2024.1.1.11-windows.exe)

#### Backend
> ⚠️ 아래 순서대로 진행하세요. 특히 `npm install`과 Firebase 비공개 키(아래 *Post-Setup Configuration* 참고)는 서버 실행 전 반드시 필요합니다. 모든 명령은 `backend/` 디렉터리 안에서 실행합니다.
```
$ git clone https://github.com/pill27211/Chat-east.git
$ cd Chat-east/backend

# 1) 의존성 설치
$ npm install

# 2) MySQL 설치 & 실행
$ sudo apt update
$ sudo apt install mysql-server
$ sudo mysql_secure_installation
$ sudo service mysql start

# 3) DB · 계정 생성
$ sudo mysql -e "CREATE DATABASE chat_east CHARACTER SET utf8mb4;"
$ sudo mysql -e "CREATE USER 'root'@'localhost' IDENTIFIED BY '0000';"
$ sudo mysql -e "GRANT ALL PRIVILEGES ON chat_east.* TO 'root'@'localhost';"
$ sudo mysql -e "FLUSH PRIVILEGES;"

# 4) 스키마(테이블) 적재
$ sudo mysql chat_east < utils/db_schema.sql

# 5) 환경변수: 예시를 복사한 뒤 값을 채웁니다
$ cp utils/.env.example utils/.env

# 6) Firebase 비공개 키 배치 (아래 Post-Setup Configuration 참고 — 없으면 서버가 부팅되지 않습니다)

# --- 실행 (반드시 backend/ 안에서) ---
# Run Foreground
$ npx nodemon index.js

# Run Background
$ npm install -g pm2
$ pm2 start index.js --name backend-server
```


#### Frontend
```
$ git clone https://github.com/pill27211/Chat-east.git
```
#### Post-Setup Configuration
```
# Backend
※ 아래 1~3(Firebase 비공개 키)은 서버를 실행하기 전에 완료해야 합니다. 키가 없으면 서버가 부팅되지 않습니다.
1. Firebase에서 프로젝트를 생성합니다.
2. 서비스 계정 - Firebase Admin SDK에서 새 비공개 키를 생성합니다.
3. 생성된 json 파일의 이름을 'service_account_key'로 변경한 뒤 'backend/utils/' 에 붙여넣습니다. (`.gitignore`에 등록되어 있어 커밋되지 않습니다.)
4. 로컬 호스트가 아닌 곳에 배포를 하고자 한다면, 적절한 포트 포워딩이 필요할 수 있습니다. 'backend/utils/.env'를 참고하세요.

# Frontend
1. 생성한 Firebase 프로젝트에 Chat-east 앱을 등록합니다.
2. 프로젝트 설정 - 일반 - 내 앱에서 접속할 곳의 SHA1(SHA 256) 키를 등록합니다.
3. 생성된 google-services.json 파일을 'android/Chat_east/app/'에 붙여넣습니다.
```
---
## Stacks 🐈

### Environment

![Android Studio](https://img.shields.io/badge/Android%20Studio-34A853?style=for-the-badge&logo=Android&logoColor=white)
![Git](https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=Git&logoColor=white)
![Ubuntu](https://img.shields.io/badge/Ubuntu-E95420?style=for-the-badge&logo=Ubuntu&logoColor=white)

### Cloud

![FireBase](https://img.shields.io/badge/FireBase-DD2C00?style=for-the-badge&logo=FireBase&logoColor=white)
![GoogleCloud](https://img.shields.io/badge/google%20cloud-4285F4?style=for-the-badge&logo=googlecloud&logoColor=white)

### Development

![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=Javascript&logoColor=white)
![Java](https://img.shields.io/badge/java-007396?style=for-the-badge&logo=java&logoColor=white)
![Node.js](https://img.shields.io/badge/Node.js-339933?style=for-the-badge&logo=Node.js&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=MySQL&logoColor=white)
![SQLite](https://img.shields.io/badge/SQLite-003B57?style=for-the-badge&logo=SQLite&logoColor=white)

### API

![Socket.io](https://img.shields.io/badge/Socket.IO-010101?style=for-the-badge&logo=socketdotio&logoColor=white)
![WebRTC](https://img.shields.io/badge/WebRTC-333333?style=for-the-badge&logo=WebRTC&logoColor=white)
![FCM](https://img.shields.io/badge/FCM-DD2C00?style=for-the-badge&logo=FireBase&logoColor=white)
![Retrofit2](https://img.shields.io/badge/Retrofit2-48B983?style=for-the-badge&logo=Retrofit2&logoColor=white)
![REST](https://img.shields.io/badge/REST-01B5E6?style=for-the-badge&logo=REST&logoColor=white)

### Communication

![Github](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=GitHub&logoColor=white)
![DisCord](https://img.shields.io/badge/DisCord-5865F2?style=for-the-badge&logo=DisCord&logoColor=white)
![KakaoTalk](https://img.shields.io/badge/KakaoTalk-FFCD00?style=for-the-badge&logo=KakaoTalk&logoColor=white)

---
## 화면 구성 📺

<details>
<summary>친구 화면</summary>

<div align="center">

| 친구 리스트 뷰  |  프로필 설정 뷰   | 친구 태그로 친구 추가 뷰 |
| :-------------------------------------------: | :------------: | :------------: |
|  <img width="225" src="https://github.com/pill27211/Chat-east/blob/master/android/Image/Main_Friend.jpg"/> |  <img width="225" src="https://github.com/pill27211/Chat-east/blob/master/android/Image/Edit_Profile.jpg"/> | <img width="225" src="https://github.com/pill27211/Chat-east/blob/master/android/Image/Add_Friend_Tag.jpg"/> |

</div>

</details>

<details>
<summary>채팅 화면</summary>

<div align="center">

| 채팅 리스트 뷰  |  채팅 방 뷰  | 채팅방 알림 기능 뷰 |
| :-------------------------------------------: | :------------: | :------------: |
|  <img width="225" src="https://github.com/pill27211/Chat-east/blob/master/android/Image/Add_Friend_Tag.jpg"/> |  <img width="225" src="https://github.com/pill27211/Chat-east/blob/master/android/Image/Chatting_Room.jpg"/>| <img width="225" src="https://github.com/pill27211/Chat-east/blob/master/android/Image/Notification.jpg"/> |
|  채팅 사이드바 뷰  | 전화 뷰 |  사진 앨범 뷰  |
|  <img width="225" src="https://github.com/pill27211/Chat-east/blob/master/android/Image/Chatting_Sidebar.jpg"/>  | <img width="225" src="https://github.com/pill27211/Chat-east/blob/master/android/Image/Connect_Call.jpg"/>   |  <img width="225" src="https://github.com/pill27211/Chat-east/blob/master/android/Image/Image_Album.jpg"/>  |
| 친구 초대 뷰 |  채팅방 만들기 뷰  | 채팅방 부가기능 뷰 |
| <img width="225" src="https://github.com/pill27211/Chat-east/blob/master/android/Image/Invite_Friend.jpg"/>   |  <img width="225" src="https://github.com/pill27211/Chat-east/blob/master/android/Image/Make_Chatroom.jpg"/>  |  <img width="225" src="https://github.com/pill27211/Chat-east/blob/master/android/Image/Chatting_Plus.jpg"/>  |

</div>

</details>

<details>
<summary>설정 화면</summary>

<div align="center">

| 설정 리스트 뷰  |  알림 설정 뷰  |
| :-------------------------------------------: | :------------: |
|  <img width="225" src="https://github.com/pill27211/Chat-east/blob/master/android/Image/Main_Setting.jpg"/> |  <img width="225" src="https://github.com/pill27211/Chat-east/blob/master/android/Image/Notification_Setting.jpg"/>|

</div>

</details>

<details>
<summary>전체 영상</summary>

<div align="center">

<a href="https://youtu.be/KcvXNEZWzU0" target="_blank">
  <img src="https://github.com/pill27211/Chat-east/blob/master/android/Image/Preview.png" alt="Video Label" width="500"/>
</a>

</div>

</details>

---
## 주요 기능 📦

### ⭐️ 유저 태그로 친구 추가 기능
- 친구의 태그를 검색해서 친구 추가 / 차단 / 1:1 대화 가능
- 원하는 친구와 채팅방 개설 / 채팅방에서 친구 초대 가능

### ⭐️ 자유로운 프로필 변경 가능
- 프로필 이미지 / 배경 이미지 / 한줄 소개를 자유롭게 변경 가능

### ⭐️ 다양한 부가 기능
- 사진 / 음성 / 파일 전송 & 다운로드 가능
- 채팅방에 전송된 사진을 원본 화질로 다운로드 / 열람 가능
- 채팅방에 전송되었던 사진들을 한눈에 채팅방 앨범에서 열람 가능
- 통화 기능을 통해 원하는 사람과 1:1 통화 가능
- 알림 기능을 통해 실시간 채팅현황 확인가능 / 해당 채팅이 온 채팅방으로 바로 이동 가능

---

## 아키텍쳐

### 디렉토리 구조
```bash
backend : 백엔드 (서버)
├── node_modules
├── socket_events : 클라이언트와 주고받는 소켓 이벤트들이 정의된 폴더
│   ├── chatroom.js
│   ├── disconnect.js
│   ├── friend.js
│   ├── login_success.js
│   ├── message.js
│   ├── profile.js
│   └── setting.js
├── upload_files : 클라이언트에서 업로드한 파일이 실제 저장되는 폴더
├── utils : 그 외 기타 보조 폴더
│   ├── .env : 서버 환경 변수
│   ├── db_schema.png
│   ├── db_schema.sql : DDL(MYSQL)
│   ├── file_upload.js
│   ├── firebase.js : 클라이언트 푸시 알림을 위한 Firebase Admin SDK 초기화
│   ├── functions.js
│   └── service_account_key.json
├── README.md
├── error_log.txt : DB 쿼리 에러 발생 시 자동으로 기록되는 로그 파일
├── index.js : 서버 메인 구동 파일
├── package-lock.json
└── package.json


android : 프론트엔드 (앱)
├── README.md
├── image : Chat-east 소개용 이미지 폴더
├── Chat_East
│   ├── .gitignore
│   ├── build.gradle.kts
│   ├── gradle.properties
│   └── gradle
│       ├── wrapper
│       └── libs.versions.toml
└── app
    ├── .gitignore
    ├── build.gradle.kts
    ├── google-services.json
    └── src/main
        ├── AndroidManifest.xml
        ├── AndroidManifest.xml
        ├── res : 디자인 관련 폴더
        └── java/com/example/chat_east : 화면에 표시되는 뷰와 관련된 레이아웃 및 컴포넌트를 관리하는 폴더
            ├── add_friend.java
            ├── call.java
            ├── chat_db_helper.java
            ├── chat_db_manager.java
            ├── chatroom_img.java
            ├── chatroom_img_adapter.java
            ├── chatroom_invite.java
            ├── chatroom_list_adapter.java
            ├── chatting_room.java
            ├── create_chatroom.java
            ├── firebase_message_service.java
            ├── friend_db_helper.java
            ├── friend_db_manager.java
            ├── friend_list_adapter.java
            ├── image_util.java
            ├── image_util_download.java
            ├── image_util_profile.java
            ├── invite_friend_list_adapter.java
            ├── invite_friend_search_adapter.java
            ├── login_activity.java
            ├── login_check.java
            ├── main_chatting.java
            ├── main_friend.java
            ├── main_setting.java
            ├── message_adapter.java
            ├── message_receive.java
            ├── notification_check.java
            ├── photo_view.java
            ├── profile_adapter.java
            ├── profile_view.java
            ├── save_image_task.java
            ├── save_image_task_download.java
            ├── save_image_task_profile.java
            ├── side_bar_friend_list_adapter.java
            ├── api : 서버와 REST API 통신을 하기 위한 인터페이스들이 정의된 폴더
            │   ├── retrofit2_api_chatroom.java
            │   ├── retrofit2_api_profile.java
            │   └── retrofit2_api_service.java
            └── setting : 설정 뷰와 관련된 레이아웃 및 컴포넌트를 관리하는 폴더
                 ├── friends_activity.java
                 ├── logout_activity.java
                 ├── notifications_activity.java
                 └── profile_activity.java
```

---

**© 2024 Chat-East 팀** — 이진수([@kiwijuse](https://github.com/kiwijuse)) · 정필선([@pill27211](https://github.com/pill27211))

협성대학교 소프트웨어학과 졸업작품(2인 팀)입니다. 각 기여자에게 권리가 있으며, 사용 문의는 팀에 주세요.
