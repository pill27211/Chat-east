const moment = require('moment-timezone');

module.exports = function(io, socket, db_api, functions) {
    socket.on('update_nickname', function(data) {
        console.log('update_nickname', data.user_id, data.nickname);

        let query = "update users set nickname = ?, last_profile_update = now() where user_id = ?";
        db_api.query(query, [data.nickname, data.user_id], (err, result) => {
            if (err) {
                functions.WriteErrorLog(`[${__filename}] - [${query}] : ${ err.message }`);
                return;
            }

            query = "select uc.chatroom_id from userchatrooms uc join chatrooms c on uc.chatroom_id = c.chatroom_id where user_id = ? and join_type = 1 and c.chatroom_type != 3";
            db_api.query(query, [data.user_id], (err, result) => {
                if (err) {
                    functions.WriteErrorLog(`[${__filename}] - [${query}] : ${ err.message }`);
                    return;
                }

                let chatroom_id_list = result.map(row => row.chatroom_id);
                chatroom_id_list.forEach(chatroom_id => {
                    query = "select u.nickname from users u join userchatrooms uc on u.user_id = uc.user_id where uc.chatroom_id = ? and uc.join_type = 1";
                    db_api.query(query, [chatroom_id],  (err, result) => {
                        if (err) {
                            functions.WriteErrorLog(`[${__filename}] - [${query}] : ${ err.message }`);
                            return;
                        }

                        let nickname_list = result.map(row => row.nickname);
                        let new_chatroom_name = nickname_list.join(', ');

                        query = "update chatrooms set chatroom_name = ?, updated_time = now() where chatroom_id = ?";
                        db_api.query(query, [new_chatroom_name, chatroom_id]);
                    });
                });
            });
        });
    });

    socket.on('update_comment', function(data) {
        console.log('update_comment', data.user_id, data.comment);

        let query = "update users set comment = ?, last_profile_update = now() where user_id = ?";
        db_api.query(query, [data.comment, data.user_id], (err, result) => {
            if (err) {
                functions.WriteErrorLog(`[${__filename}] - [${query}] : ${ err.message }`);
                return;
            }
        });
    });

    socket.on('update_user_tag', function(data) {
        let query = "select count(*) as count from users where user_tag = ?";
        db_api.query(query, [data.user_tag], (err, result) => {
            if (err) {
                functions.WriteErrorLog(`[${__filename}] - [${query}] : ${ err.message }`);
                return;
            }

            if (result[0].count == 0) {
                console.log('update_user_tag', data.user_id, data.user_tag, "OK");
                query = "update users set user_tag = ? where user_id = ?";
                db_api.query(query, [data.user_tag, data.user_id], (err, result) => {
                    if (err) {
                        functions.WriteErrorLog(`[${__filename}] - [${query}] : ${ err.message }`);
                        return;
                    }
                });
            } else {
                console.log('update_user_tag', data.user_id, data.user_tag, "XX");
            }
        });
    });
};