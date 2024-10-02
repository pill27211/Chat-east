module.exports = function(io, socket, db_api, functions) {
    socket.on('chatroom_list_update', function(data) {
        console.log('chatroom_list_update', data.user_id, data.update_time);

        query = "select c.chatroom_id, c.chatroom_name, c.chatroom_type, uc.favorite_type, uc.notification_type, uc.last_view_time, c.chatroom_img_url, c.updated_time as update_time, c.people_count, m.content as last_message, m.create_time as last_message_time, m.message_type from userchatrooms uc join chatrooms c on uc.chatroom_id = c.chatroom_id left join messages m on c.last_message_id = m.message_id where uc.user_id = ? and (c.updated_time > ? or uc.last_view_time > ? or uc.update_time > ?) and uc.join_type = 1";
        db_api.query(query, [data.user_id, data.update_time, data.update_time, data.update_time], (err, result) => {
            if (err) {
                functions.WriteErrorLog(`[${__filename}] - [${query}] : ${ err.message }`);
                return;
            }

            if(result.length === 0) {
                socket.emit('chatroom_list_update', { chatroom_list_data: null });
                return;
            }

            result.forEach(chatroom_list_data => {
                if(chatroom_list_data.message_type === 2) {
                    chatroom_list_data.last_message = "사진을 보냈습니다.";
                }
                if (chatroom_list_data.last_view_time) chatroom_list_data.last_view_time = functions.GetTime(chatroom_list_data.last_view_time);
                if (chatroom_list_data.update_time) chatroom_list_data.update_time = functions.GetTime(chatroom_list_data.update_time);
                if (chatroom_list_data.last_message_time) chatroom_list_data.last_message_time = functions.GetTime(chatroom_list_data.last_message_time);
            });

            socket.emit('chatroom_list_update', { chatroom_list_data: result });
        });
    });

    socket.on('last_view_time', function(data) {
        const now_time = functions.GetTime();
        console.log('last_view_time_update', data.user_id, data.chatroom_id, now_time);

        query = "update userchatrooms set last_view_time = ? where user_id = ? and chatroom_id = ?";
        db_api.query(query, [now_time, data.user_id, data.chatroom_id], (err, result) => {
            if (err) {
                functions.WriteErrorLog(`[${__filename}] - [${query}] : ${ err.message }`);
                return;
            }
        });
    });

    socket.on('enter_chatroom', function(data) {
        console.log('enter_chatroom', data.user_id, data.chatroom_id, data.update_time);

        query = "select count(*) as count from test where user_id = ? and chatroom_id = ?";
        db_api.query(query, [data.user_id, data.chatroom_id], (err, result) => {
            if (result[0].count === 0) {
                query = "insert into test(user_id, chatroom_id, socket_id) values(?, ?, ?)";
                db_api.query(query, [data.user_id, data.chatroom_id, socket.id]);
            }
            else {
                query = "update test set socket_id = ? where user_id = ? and chatroom_id = ?";
                db_api.query(query, [socket.id, data.user_id, data.chatroom_id]);
            }

            query = "select u.user_id, u.nickname, u.comment, u.background_img_url, u.profile_img_url, coalesce(f.friend_type, 0) as friends_type, cast(greatest(u.last_profile_update, coalesce(f.update_time, now()), coalesce(uc.update_time, '2000-08-26 16:08:35')) as datetime) as update_time, case when f.friend_type is not null then 1 else 0 end as flag, uc.join_type from users u join userchatrooms uc on u.user_id = uc.user_id left join friends f on f.to_user_id = u.user_id and f.from_user_id = ? where uc.chatroom_id = ? and uc.user_id != ? and (u.last_profile_update > ? || coalesce(f.update_time, now()) > ? || uc.update_time > ?)";
            db_api.query(query, [data.user_id, data.chatroom_id, data.user_id, data.update_time, data.update_time, data.update_time], (err, result) => {
                if (err) {
                    functions.WriteErrorLog(`[${__filename}] - [${query}] : ${ err.message }`);
                    return;
                }

                result.forEach(friends_data => {
                    friends_data.update_time = functions.GetTime(friends_data.update_time);

                    if (friends_data.flag === 0) {
                        query = "insert into friends(from_user_id, to_user_id, friend_type, update_time) values(?, ?, 0, now())";
                        db_api.query(query, [data.user_id, friends_data.user_id]);
                    }
                });

                socket.join(data.chatroom_id);
                socket.emit('enter_chatroom', { friends_data: result });
            });
        });
    });

    socket.on('create_chatroom', function(data) {
        const now_time = functions.GetTime();
        console.log('create_chatroom', data.people_count);

        if(data.people_count === 2) {
            query = "select uc.chatroom_id from chatrooms c join userchatrooms uc on c.chatroom_id = uc.chatroom_id where c.people_count = 2 and uc.user_id in (?, ?) group by uc.chatroom_id having count(uc.user_id) = 2";
            db_api.query(query, [data.user_id_list[0], data.user_id_list[1]], (err, result) => {
                if (err) {
                    functions.WriteErrorLog(`[${__filename}] - [${query}] : ${ err.message }`);
                    return;
                }

                const chatroom_name = `${data.my_nickname}, ${data.friend_nickname}`;

                if (result.length > 0) {
                    const chatroom_id = result[0].chatroom_id;
                    socket.emit('create_chatroom', { chatroom_id: chatroom_id, chatroom_name: chatroom_name });
                    return;
                }

                query = "insert into chatrooms(chatroom_name, updated_time, people_count, chatroom_type) values(?, now(), ?, 2)";
                db_api.query(query, [chatroom_name, data.people_count], (err, result) => {
                    if (err) {
                        functions.WriteErrorLog(`[${__filename}] - [${query}] : ${ err.message }`);
                        return;
                    }

                    const chatroom_id = result.insertId;

                    data.user_id_list.forEach(user_id => {
                        query = "insert into userchatrooms(user_id, chatroom_id, is_typing, favorite_type, join_type, update_time, notification_type) values(?, ?, 0, 1, 1, now(), 1)";
                        db_api.query(query, [user_id, chatroom_id]);
                    });

                    socket.emit('create_chatroom', { chatroom_id: chatroom_id, chatroom_name: chatroom_name });
                });
            });
        }
        else if(data.people_count === 1) {
            query = "select uc.chatroom_id from chatrooms c join userchatrooms uc on c.chatroom_id = uc.chatroom_id where c.people_count = 1 and uc.user_id in (?) group by uc.chatroom_id having count(uc.user_id) = 1";
            db_api.query(query, [data.user_id_list[0]], (err, result) => {
                if (err) {
                    functions.WriteErrorLog(`[${__filename}] - [${query}] : ${ err.message }`);
                    return;
                }
            
                if (result.length > 0) {
                    const chatroom_id = result[0].chatroom_id;
                    socket.emit('create_chatroom', { chatroom_id: chatroom_id, chatroom_name: data.my_nickname });
                    return;
                }

                query = "insert into chatrooms(chatroom_name, updated_time, people_count, chatroom_type) values(?, now(), ?, 1)";
                db_api.query(query, [data.my_nickname, data.people_count], (err, result) => {
                    if (err) {
                        functions.WriteErrorLog(`[${__filename}] - [${query}] : ${ err.message }`);
                        return;
                    }

                    const chatroom_id = result.insertId;

                    data.user_id_list.forEach(user_id => {
                        query = "insert into userchatrooms(user_id, chatroom_id, is_typing, favorite_type, join_type, update_time, notification_type) values(?, ?, 0, 1, 1, now(), 1)";
                        db_api.query(query, [user_id, chatroom_id]);
                    });

                    socket.emit('create_chatroom', { chatroom_id: chatroom_id, chatroom_name: data.my_nickname });
                });
            });
        }
        else {
            people_count = data.people_count - 1;
            query = "insert into chatrooms(chatroom_name, updated_time, people_count, chatroom_type) values(?, now(), ?, 3)";
            db_api.query(query, [data.chatroom_name, people_count], (err, result) => {
                if (err) {
                    functions.WriteErrorLog(`[${__filename}] - [${query}] : ${ err.message }`);
                    return;
                }

                const chatroom_id = result.insertId;

                data.user_id_list.forEach(user_id => {

                    query = "insert into userchatrooms(user_id, chatroom_id, is_typing, favorite_type, join_type, update_time, notification_type) values(?, ?, 0, 1, 1, now(), 1)";
                    db_api.query(query, [user_id, chatroom_id]);
                });

                socket.emit('create_chatroom', { chatroom_id: chatroom_id, chatroom_name: data.chatroom_name });
            });
        }
    });

    socket.on('leave_chatroom', function(data) {
        const now_time = functions.GetTime();
        console.log('leave_chatroom', data.user_id, data.chatroom_id, now_time);

        query = "select people_count from chatrooms where chatroom_id = ?";
        db_api.query(query, [data.chatroom_id], (err, result) => {
            if (err) {
                functions.WriteErrorLog(`[${__filename}] - [${query}] : ${ err.message }`);
                return;
            }

            const people_count = result[0].people_count;

            query = "update userchatrooms set join_type = 0, update_time = ? where user_id = ? and chatroom_id = ?";
            db_api.query(query, [now_time, data.user_id, data.chatroom_id], (err, result) => {
                if (err) {
                    functions.WriteErrorLog(`[${__filename}] - [${query}] : ${ err.message }`);
                    return;
                }

                if (people_count > 1) {
                    query = "select nickname from users where user_id in (select user_id from userchatrooms where chatroom_id = ? and join_type = 1)";
                    db_api.query(query, [data.chatroom_id], (err, result) => {
                        if (err) {
                            functions.WriteErrorLog(`[${__filename}] - [${query}] : ${ err.message }`);
                            return;
                        }

                        const nicknames = result.map(row => row.nickname);
                        const new_chatroom_name = nicknames.join(', ');

                        query = "update chatrooms set chatroom_name = ?, people_count = people_count - 1, updated_time = now() where chatroom_id = ?";
                        db_api.query(query, [new_chatroom_name, data.chatroom_id]);

                        query = "delete from test where user_id = ? and chatroom_id = ?";
                        db_api.query(query, [data.user_id, data.chatroom_id]);
                    });
                }
                else {
                    query = "delete from messages where chatroom_id = ?";
                    db_api.query(query, [data.chatroom_id]);

                    query = "delete from test where chatroom_id = ?";
                    db_api.query(query, [data.chatroom_id]);

                    query = "delete from userchatrooms where chatroom_id = ?";
                    db_api.query(query, [data.chatroom_id]);

                    query = "delete from chatrooms where chatroom_id = ?";
                    db_api.query(query, [data.chatroom_id]);
                }
            });
        });
    });

    socket.on('favorite_type_update', function(data) {
        const now_time = functions.GetTime();
        console.log('favorite_type_update', data.user_id, data.chatroom_id, data.favorite_type);

        query = "update userchatrooms set favorite_type = ? where user_id = ? and chatroom_id = ?";
        db_api.query(query, [data.favorite_type, data.user_id, data.chatroom_id], (err, result) => {
            if (err) {
                functions.WriteErrorLog(`[${__filename}] - [${query}] : ${ err.message }`);
                return;
            }
        });
    });
    socket.on('notification_type_update', function(data) {
        const now_time = functions.GetTime();
        console.log('notification_type_update', data.user_id, data.chatroom_id, data.notification_type);

        query = "update userchatrooms set notification_type = ? where user_id = ? and chatroom_id = ?";
        db_api.query(query, [data.notification_type, data.user_id, data.chatroom_id], (err, result) => {
            if (err) {
                functions.WriteErrorLog(`[${__filename}] - [${query}] : ${ err.message }`);
                return;
            }
        });
    });
/////////////////////////////////////////////////////
    socket.on('invite_user', function(data) {
        const now_time = functions.GetTime();
        console.log('invite_user', data.user_id, data.chatroom_id);

        let query = "select chatroom_name, chatroom_type, people_count from chatrooms where chatroom_id = ?";
        db_api.query(query, [data.chatroom_id], (err, result) => {
            if (err) {
                functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                return;
            }

            const chatroom_name = result[0].chatroom_name;
            const chatroom_type = result[0].chatroom_type;
            const people_count = result[0].people_count;
            const new_people_count = people_count + data.friend_list.length;

            if (chatroom_type === 2) {
                const new_chatroom_name = [chatroom_name, ...data.user_nickname_list].join(', ');

                query = "insert into chatrooms(chatroom_name, updated_time, people_count, chatroom_type) values(?, now(), ?, 4)";
                db_api.query(query, [new_chatroom_name, new_people_count], (err, result) => {
                    if (err) {
                        functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                        return;
                    }

                    const new_chatroom_id = result.insertId;

                    data.friend_list.forEach(friend_id => {
                        query = "insert into userchatrooms(user_id, chatroom_id, is_typing, favorite_type, join_type, update_time, notification_type) values(?, ?, 0, 1, 1, now(), 1)";
                        db_api.query(query, [friend_id, new_chatroom_id], (err, result) => {
                            if (err) {
                                functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                            }
                        });
                    });

                    query = "select user_id from userchatrooms where chatroom_id = ? and join_type = 1";
                    db_api.query(query, [data.chatroom_id], (err, result) => {
                        if (err) {
                            functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                            return;
                        }
  
                        result.forEach(row => {
                            const existing_user_id = row.user_id;

                            query = "insert into userchatrooms(user_id, chatroom_id, is_typing, favorite_type, join_type, update_time, notification_type) VALUES(?, ?, 0, 1, 1, now(), 1)";
                            db_api.query(query, [existing_user_id, new_chatroom_id], (err, result) => {
                                if (err) {
                                    functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                                }
                            });    
                        });

                        socket.emit('invite_user', { chatroom_id: new_chatroom_id, people_count: new_people_count });
                    });
                });
            }
            else if (chatroom_type === 3) {
                data.friend_list.forEach(friend_id => {
                    query = "select count(*) as count from userchatrooms where user_id = ? and chatroom_id = ?";
                    db_api.query(query, [friend_id, data.chatroom_id], (err, result) => {
                         if (err) {
                            functions.WriteErrorLog(`[${__filename}] - [${checkQuery}] : ${err.message}`);
                            return;
                        }
            
                        const count = result[0].count;
            
                        if (count > 0) {
                            query = "update userchatrooms set join_type = 1, update_time = now() where user_id = ? and chatroom_id = ?";
                            db_api.query(query, [friend_id, data.chatroom_id], (err, result) => {
                                if (err) {
                                    functions.WriteErrorLog(`[${__filename}] - [${updateQuery}] : ${err.message}`);
                                }
                            });
                        }
                        else {
                            query = "insert into userchatrooms(user_id, chatroom_id, is_typing, favorite_type, join_type, update_time, notification_type) values(?, ?, 0, 1, 1, now(), 1)";
                            db_api.query(query, [friend_id, data.chatroom_id], (err, result) => {
                                if (err) {
                                    functions.WriteErrorLog(`[${__filename}] - [${insertQuery}] : ${err.message}`);
                                }
                            });
                        }
                    });
                });

                query = "update chatrooms set updated_time = now(), people_count = new_people_count where chatroom_id = ?";
                db_api.query(query, [new_people_count, data.chatroom_id, data.chatroom_id], (err, result) => {
                    if (err) {
                        functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                        return;
                    }

                    socket.emit('invite_user', { chatroom_id: 0, people_count: new_people_count });
                });
            }
            else if (chatroom_type === 4) {
                const new_chatroom_name = [chatroom_name, ...data.user_nickname_list].join(', ');

                data.friend_list.forEach(friend_id => {
                    query = "insert into userchatrooms(user_id, chatroom_id, is_typing, favorite_type, join_type, update_time, notification_type) values(?, ?, 0, 1, 1, now(), 1)";
                    db_api.query(query, [friend_id, data.chatroom_id], (err, result) => {
                        if (err) {
                            functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                        }
                    });
                });

                query = "update chatrooms set chatroom_name = ?, people_count = new_people_count, updated_time = now() where chatroom_id = ?";
                db_api.query(query, [new_chatroom_name, data.chatroom_id]);

                socket.emit('invite_user', { chatroom_id: data.chatroom_id, people_count: new_people_count });
            }
        });
    });

/////////////////////////////////////////////////////
    socket.on('notification_type_update', function(data) {
        const now_time = functions.GetTime();
        console.log('notification_type_update', data.user_id, data.chatroom_id, data.notification_type);

        query = "update userchatrooms set notification_type = ? where user_id = ? and chatroom_id = ?";
        db_api.query(query, [data.notification_type, data.user_id, data.chatroom_id], (err, result) => {
            if (err) {
                functions.WriteErrorLog(`[${__filename}] - [${query}] : ${ err.message }`);
                return;
            }
        });
    });


    socket.on('get_message', function(data) {
        console.log('get_message', data.chatroom_id, data.update_time);

        query = "select m.user_id, m.message_id, m.chatroom_id, m.content, m.message_type, m.create_time, m.update_time, COALESCE(i.width, 0) as width, COALESCE(i.height, 0) as height, m.sub_content as file_name, m.content_size as file_size from messages m left join image_info i on m.message_id = i.message_id where m.chatroom_id = ? and m.update_time >= ?";
        db_api.query(query, [data.chatroom_id, data.update_time], (err, result) => {
            if (err) {
                functions.WriteErrorLog(`[${__filename}] - [${query}] : ${ err.message }`);
                return;
            }

            result.forEach(message_list => {
                message_list.create_time = functions.GetTime(message_list.create_time);
                message_list.update_time = functions.GetTime(message_list.update_time);
            });

            if(result.length === 0) {
                socket.emit('get_message', { message_list: null });
                return
            }

            socket.emit('get_message', { message_list: result });
        });
    });


    function ChatroomsGrouping(user_id) {
        console.log('ChatroomGrouping', user_id);

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