CREATE TABLE chatrooms (
    chatroom_id INT NOT NULL AUTO_INCREMENT,
    chatroom_name VARCHAR(256) NOT NULL,
    updated_time DATETIME NOT NULL,
    chatroom_img_url VARCHAR(256),
    people_count INT DEFAULT 1,
    last_message_id INT,
    chatroom_type INT DEFAULT 0,
    PRIMARY KEY (chatroom_id)
);

CREATE TABLE friends (
    friend_id INT NOT NULL AUTO_INCREMENT,
    from_user_id INT NOT NULL,
    to_user_id INT NOT NULL,
    friend_type INT NOT NULL,
    update_time DATETIME,
    PRIMARY KEY (friend_id)
);

CREATE TABLE messages (
    message_id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    nickname VARCHAR(64),
    chatroom_id INT NOT NULL,
    content LONGTEXT,
    message_type INT NOT NULL,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    sub_content LONGTEXT,
    content_size INT DEFAULT 0,
    PRIMARY KEY (message_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (chatroom_id) REFERENCES chatrooms(chatroom_id)
);

CREATE TABLE users (
    user_id INT NOT NULL AUTO_INCREMENT,
    nickname VARCHAR(64),
    email VARCHAR(100) NOT NULL,
    email_company VARCHAR(64) NOT NULL,
    user_tag VARCHAR(64),
    account_status INT NOT NULL,
    login_method TINYINT(1) NOT NULL,
    language TINYINT(1) NOT NULL,
    comment VARCHAR(256),
    profile_img_url VARCHAR(256),
    background_img_url VARCHAR(256),
    last_profile_update DATETIME,
    fcm_token VARCHAR(1000),
    PRIMARY KEY (user_id)
);

CREATE TABLE userchatrooms (
    ucroom_id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    chatroom_id INT NOT NULL,
    is_typing TINYINT(1) NOT NULL,
    favorite_type INT DEFAULT 0,
    last_view_time DATETIME,
    join_type INT,
    update_time DATETIME,
    notification_type INT,
    PRIMARY KEY (ucroom_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (chatroom_id) REFERENCES chatrooms(chatroom_id)
);

CREATE TABLE image_info (
    message_id INT NOT NULL,
    width INT,
    height INT,
    PRIMARY KEY (message_id),
    FOREIGN KEY (message_id) REFERENCES messages(message_id)
);

CREATE TABLE voice_talk (
    voice_talk_code INT DEFAULT 0,
    user_id INT NOT NULL,
    socket_id VARCHAR(256) NOT NULL,
    PRIMARY KEY (user_id)
);

CREATE TABLE test (
    user_id INT NOT NULL,
    chatroom_id INT NOT NULL,
    socket_id TEXT,
    PRIMARY KEY (user_id, chatroom_id)
);
