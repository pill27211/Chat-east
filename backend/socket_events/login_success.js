module.exports = function(socket, db_api, functions) {
    socket.on('login_success', function(data) {
        console.log('login_success', data.email, data.fcm_token);

        let query = "select count(*) as count from users where email = ? and email_company = ?";
        db_api.query(query, [data.email, data.email_company], (err, result) => {
            if (err) {
                functions.WriteErrorLog(`[${__filename}] - [${query}] : ${ err.message }`);
                return;
            }

            // 서버에 처음 등장한 이메일이라면
            if (result[0].count == 0) {
                query = "insert into users(nickname, email, email_company, account_status, login_method, language, last_profile_update) values (?, ?, ?, 1, 0, 1, now())";
                db_api.query(query, [data.nickname, data.email, data.email_company], (err, result) => {
                    if (err) {
                        functions.WriteErrorLog(`[${__filename}] - [${query}] : ${ err.message }`);
                        return;
                    }
                });
            }

            query = "select user_id as user_id from users where email = ? and email_company = ?";
            db_api.query(query, [data.email, data.email_company], (err, result) => {
                if (err) {
                    functions.WriteErrorLog(`[${__filename}] - [${query}] : ${ err.message }`);
                    return;
                }

                const user_id = result[0].user_id;

                //fcm 토큰 업데이트
                query = "update users set fcm_token = NULL where fcm_token = ?";
                db_api.query(query, [data.fcm_token]);

                query = "update users set fcm_token = ? where user_id = ?";
                db_api.query(query, [data.fcm_token, user_id]);

                // 최신 메시지
                query = "select m.user_id, m.message_id, m.chatroom_id, m.content, m.message_type, m.create_time, m.update_time, COALESCE(i.width, 0) as width, COALESCE(i.height, 0) as height, m.sub_content as file_name, m.content_size as file_size from messages m left join image_info i on m.message_id = i.message_id where m.chatroom_id in (select chatroom_id from userchatrooms where user_id = ?) and m.update_time > ?";
                db_api.query(query, [user_id, data.last_message_time], (err, result) => {
                    if (err) {
                        functions.WriteErrorLog(`[${__filename}] - [${query}] : ${ err.message }`);
                        return;
                    }

                    const recent_messages = result.map(message => ({
                        ...message,
                        create_time: functions.GetTime(message.create_time),
                        update_time: functions.GetTime(message.update_time),
                    }));


                    // 닉네임 조회
                    query = "select nickname, user_tag from users where user_id = ?";
                    db_api.query(query, [user_id], (err, result) => {
                        if (err) {
                            functions.WriteErrorLog(`[${__filename}] - [${query}] : ${ err.message }`);
                            return;
                        }

                        const nickname = result[0].nickname;
                        const user_tag = result[0].user_tag;

                        //해당 유저가 속해있는 채팅방을 순회하며 socket grouping
                        //ChatroomsGrouping(user_id);

                        socket.emit('get_message', { user_id: user_id, recent_messages: recent_messages, nickname: nickname, user_tag: user_tag });
                    });
                });
            });
        });
    });

    socket.on('receive_connect', function(data) {
        console.log('receive_connect');
        ChatroomsGrouping(data.user_id);
    });

    function ChatroomsGrouping(user_id) {
console.log('Grouping', user_id);

        query = "select chatroom_id from userchatrooms where user_id = ?";
        db_api.query(query, [user_id], (err, result) => {
            if (err) {
                functions.WriteErrorLog(`[${__filename}] - [${query}] : ${ err.message }`);
                return;
            }

            // 뽑아낸 chatroom_id를 순회하며 user를 해당 소켓 그룹에 추가
            result.forEach(row => {

                socket.join(row.chatroom_id);
                console.log(`${socket.id} joined ${row.chatroom_id}`);
            });
        });
    }
};