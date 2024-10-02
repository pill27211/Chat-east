const multer = require('multer');
const path = require('path');
const express = require('express');
const fs = require('fs');
const sharp = require('sharp');

module.exports = function(io, socket, db_api, functions, firebase_admin, app) {
    socket.on('msg', function(data) {
        const now_time = functions.GetTime();

        let query = "insert into messages(user_id, nickname, chatroom_id, content, message_type, create_time, update_time) values(?, ?, ?, ?, ?, ?, ?)";
        db_api.query(query, [data.user_id, data.nickname, data.chatroom_id, data.content, data.message_type, now_time, now_time], (err, result) => {
            if (err) {
                functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                return;
            }

            const message_id = result.insertId;

            query = "update chatrooms set updated_time = ?, last_message_id = ? where chatroom_id = ?";
            db_api.query(query, [now_time, message_id, data.chatroom_id]);

            query = "update chatrooms set updated_time = ? where chatroom_id = ?";
            db_api.query(query, [now_time, data.chatroom_id], (err, result) => {
                if (err) {
                    functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                    return;
                 }

                console.log(data.nickname, data.content);

                var message = {
                    user_id: data.user_id,
                    nickname: data.nickname,
                    chatroom_id: data.chatroom_id,
                    content: data.content,
                    message_type: data.message_type,
                    create_time: now_time,
                    update_time: now_time,
                    message_id: message_id,
                    width: 0,
                    height: 0,
                    file_name: null,
                    file_size: 0
                };

                socket.join(data.chatroom_id);
                io.to(data.chatroom_id).emit('msg_to_client', message);

                query = "select u.user_id, u.nickname, u.fcm_token, uc.notification_type, c.chatroom_type from users u join userchatrooms uc on u.user_id = uc.user_id join chatrooms c on uc.chatroom_id = c.chatroom_id where uc.chatroom_id = ? and uc.join_type = 1";
                db_api.query(query, [data.chatroom_id], (err, tokens) => {
                    if (err) {
                        functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                        return;
                    }

                    tokens.forEach(token_list => {
                        const fcm_token = token_list.fcm_token;
                        const notification_type = token_list.notification_type;
                        const user_id = token_list.user_id;
                        const nickname = token_list.nickname;
                        const chatroom_type = token_list.chatroom_type;

                        if (!fcm_token) {
                                return;
                        }

                        const sub_content = data.content.length > 64 ? data.content.substring(0, 64) + '...' : data.content;

                        const message_info = {
                            data: {
                                title: String(data.chatroom_name),
                                sender: String(data.nickname),
                                body: String(sub_content),
                                chatroom_id: String(data.chatroom_id),
                                update_time: String(now_time),
                                notification_type: String(notification_type),
                                user_id: String(user_id),
                                nickname: String(nickname),
                                chatroom_type: String(chatroom_type),
                                msg_type: String(1)
                            },
                            token: fcm_token
                        };

                        firebase_admin.messaging().send(message_info)
                            .then((response) => {
                                console.log('Successfully sent message:', fcm_token);
                            })
                            .catch((error) => {
                                console.log('Error sending message:', error);
                            });
                    });
                });
            });
        });
    });

    /////////////////////////////////////////////////////////////////////
    const images_dir = path.join(__dirname, '..', 'upload_files');

    app.use('/images', express.static(images_dir));

    const storage = multer.diskStorage({
        destination: function (req, file, cb) {
            if (!fs.existsSync(images_dir)) {
                fs.mkdirSync(images_dir, { recursive: true });
                console.log('Created image directory.', images_dir);
            }
            cb(null, images_dir);
        },
        filename: function (req, file, cb) {
            const unique_filename = Date.now() + path.extname(file.originalname);
            cb(null, unique_filename);
        }
    });

    const upload = multer({ storage: storage });

    app.post('/upload', upload.single('image'), (req, res) => {
        if (!req.file) {
            console.log('No file uploaded.');
            return res.status(400).send('No file uploaded.');
        }

        const file_url = `${req.protocol}://${req.get('host')}/images/${req.file.filename}`;
        const now_time = functions.GetTime();

        console.log('File uploaded successfully:', {
            original_name: req.file.originalname,
            size: req.file.size,
            path: req.file.path,
            filename: req.file.filename,
            file_url: file_url
        });

        console.log('Additional information received:', {
            user_id: req.body.user_id,
            chatroom_id: req.body.chatroom_id,
            message_type: req.body.message_type,
            nickname: req.body.nickname,
            chatroom_name: req.body.chatroom_name,
            path_type: req.body.path_type,
            real_file_name: req.body.real_file_name,
        });
        
        sharp(req.file.path).metadata()
            .then(metadata => {
                const { width, height, orientation } = metadata;
                let actual_width = width;
                let actual_height = height;

                switch (orientation) {
                    case 3: // 180도 회전
                    case 4: // 180도 회전(좌우 반전)
                        break;
                    case 6: // 90도 회전
                    case 8: // 270도 회전
                        actual_width = height;
                        actual_height = width;
                        break;
                    case 5: // 90도 회전(좌우 반전)
                    case 7: // 270도 회전(좌우 반전)
                        actual_width = height;
                        actual_height = width;
                        break;
                    default: // Orientation이 1, 2, 4 등인 경우 (회전 없음)
                        break;
                }

                if (req.body.path_type == 2) {
                    query = "update users set profile_img_url = ?, last_profile_update = now() where user_id = ?";
                    db_api.query(query, [file_url, req.body.user_id], (err) => {
                        if (err) {
                            functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                            return res.status(500).send('Error updating profile image.');
                        }

                        res.status(200).json({
                            message: 'Profile image updated successfully',
                            filename: req.file.filename,
                            file_url: file_url
                        });
                    });
                }
                else if (req.body.path_type == 3) {
                    query = "update users set background_img_url = ?, last_profile_update = now() where user_id = ?";
                    db_api.query(query, [file_url, req.body.user_id], (err) => {
                        if (err) {
                            functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                            return res.status(500).send('Error updating profile image.');
                        }

                        res.status(200).json({
                            message: 'Profile image updated successfully',
                            filename: req.file.filename,
                            file_url: file_url
                        });
                    });
                }
                else if (req.body.path_type == 4) {
                    query = "update chatrooms set chatroom_img_url = ?, updated_time = now() where chatroom_id = ?";
                    db_api.query(query, [file_url, req.body.chatroom_id], (err) => {
                        if (err) {
                            functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                            return res.status(500).send('Error updating profile image.');
                        }

                        res.status(200).json({
                            message: 'Profile image updated successfully',
                            filename: req.file.filename,
                            file_url: file_url
                        });
                    });
                }

                else {
                    query = "insert into messages(user_id, nickname, chatroom_id, content, message_type, create_time, update_time, sub_content) values(?, ?, ?, ?, ?, ?, ?, ?)";
                    db_api.query(query, [req.body.user_id, req.body.nickname, req.body.chatroom_id, file_url, 2, now_time, now_time, req.body.real_file_name], (err, result) => {
                        if (err) {
                            functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                            return res.status(500).send('Error inserting message.');
                        }

                        const message_id = result.insertId;

                        query = "insert into image_info values(?, ?, ?)";
                        db_api.query(query, [message_id, actual_width, actual_height]);

                        query = "update chatrooms set updated_time = ?, last_message_id = ? where chatroom_id = ?";
                        db_api.query(query, [now_time, message_id, req.body.chatroom_id]);

                        var message = {
                            user_id: req.body.user_id,
                            nickname: req.body.nickname,
                            chatroom_id: req.body.chatroom_id,
                            content: file_url,
                            message_type: req.body.message_type,
                            create_time: now_time,
                            update_time: now_time,
                            message_id: message_id,
                            width: actual_width,
                            height: actual_height,
                            file_name: req.body.real_file_name,
                            file_size: 0
                        };

                        query = "select socket_id from test where chatroom_id = ?";
                        db_api.query(query, [req.body.chatroom_id], (err, sockets) => {
                            if (err) {
                                functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                                return;
                            }

                            sockets.forEach(socket_info => {
                                const socket_id = socket_info.socket_id;
                                io.to(socket_id).emit('msg_to_client', message);
                            });

                            query = "select u.user_id, u.nickname, u.fcm_token, uc.notification_type, c.chatroom_type from users u join userchatrooms uc on u.user_id = uc.user_id join chatrooms c on uc.chatroom_id = c.chatroom_id where uc.chatroom_id = ? and uc.join_type = 1";
                            db_api.query(query, [req.body.chatroom_id], (err, tokens) => {
                                if (err) {
                                    functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                                    return;
                                }
							
                                tokens.forEach(token_list => {
                                    const fcm_token = token_list.fcm_token;
                                    const notification_type = token_list.notification_type;
                                    const user_id = token_list.user_id;
                                    const nickname = token_list.nickname;
                                    const chatroom_type = token_list.chatroom_type;

                                    if (!fcm_token) {
                                        return;
                                    }
							
                                    const message_info = {
                                        data: {
                                            title: String(req.body.chatroom_name),
                                            sender: String(req.body.nickname),
                                            body: String("사진을 보냈습니다."),
                                            chatroom_id: String(req.body.chatroom_id),
                                            update_time: String(now_time),
                                            notification_type: String(notification_type),
                                            user_id: String(user_id),
                                            nickname: String(nickname),
                                            chatroom_type: String(chatroom_type),
                                            msg_type: String(2)
                                        },
                                        token: fcm_token
                                    };

                                    firebase_admin.messaging().send(message_info)
                                        .then((response) => {
                                            console.log('Successfully sent message:', fcm_token);
                                        })
                                        .catch((error) => {
                                            console.log('Error sending message:', error);
                                        });
                                });
							
                                res.status(200).json({
                                    message: 'File uploaded successfully',
                                    filename: req.file.filename,
                                    width: actual_width,
                                    height: actual_height
                                });
                            });
                        });
                    });
                }
            })
            .catch(err => {
                functions.WriteErrorLog(`[${__filename}] - [sharp] : ${err.message}`);
                res.status(500).send('Error processing image.');
            });
    });
    /////////////////////////////////////////////////////////////////////

    socket.on('voice_talk_offer', function(data) {
console.log('voice_talk_offer', data.my_user_id, data.friend_user_id, data.my_nickname, data.friend_nickname);
        const now_time = functions.GetTime();

        query = "select c.chatroom_id from chatrooms c join userchatrooms uc1 on c.chatroom_id = uc1.chatroom_id join userchatrooms uc2 on c.chatroom_id = uc2.chatroom_id where c.chatroom_type = 2 and c.people_count = 2 and uc1.user_id = ? and uc2.user_id = ?";
        db_api.query(query, [data.my_user_id, data.friend_user_id], (err, result) => {
            if (err) {
                functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                return;
            }

            if (result.length === 0) {
                query = "insert into chatrooms(chatroom_name, updated_time, people_count, chatroom_type) values(?, ?, 2, 2)";
                db_api.query(query, [`${data.my_nickname}, ${data.friend_nickname}`, now_time], (err, result) => {
                    if (err) {
                        functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                        return;
                    }

                    query = "insert into messages(user_id, nickname, chatroom_id, content, message_type, create_time, update_time, sub_content) values(?, ?, ?, ?, 10, ?, ?, ?)";
                    db_api.query(query, [data.my_user_id, data.my_nickname, result.insertId, "보이스톡 해요.", now_time, now_time, data.friend_user_id], (err, insert_result) => {
                        if (err) {
                            functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                            return;
                        }

                        query = "update chatrooms set updated_time = ?, last_message_id = ? where chatroom_id = ?";
                        db_api.query(query, [now_time, insert_result.insertId, result.insertId]);
                    });

                    query = "select max(voice_talk_code) as max_code from voice_talk";
                    db_api.query(query, (err, result) => {
                        if (err) {
                            functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                            return;
                        }

                        const voice_talk_code = result[0].max_code + 1;

                        query = "delete from voice_talk where user_id = ?";
                        db_api.query(query, [data.my_user_id], (err) => {
                            if (err) {
                                functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                            }

                            query = "insert into voice_talk values(?, ?, ?)";
                            db_api.query(query, [voice_talk_code, data.my_user_id, socket.id], (err) => {
                                if (err) {
                                    functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                                }

                                VoiceTalkFCM(data.my_user_id, data.friend_user_id, data.my_nickname, result.insertId);
                            });
                        });
                    });
                });
            }
            else {
                const chatroom_id = result[0].chatroom_id;

                query = "insert into messages(user_id, nickname, chatroom_id, content, message_type, create_time, update_time, sub_content) values(?, ?, ?, ?, 10, ?, ?, ?)";
                db_api.query(query, [data.my_user_id, data.my_nickname, chatroom_id, "보이스톡 해요.", now_time, now_time, data.friend_user_id], (err, insert_result) => {
                    if (err) {
                        functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                        return;
                    }

                    query = "update chatrooms set updated_time = ?, last_message_id = ? where chatroom_id = ?";
                    db_api.query(query, [now_time, insert_result.insertId, chatroom_id]);
                });

                query = "select max(voice_talk_code) as max_code from voice_talk";
                db_api.query(query, (err, result) => {
                    if (err) {
                        functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                        return;
                    }

                    const voice_talk_code = result[0].max_code + 1;

                    query = "delete from voice_talk where user_id = ?";
                    db_api.query(query, [data.my_user_id], (err) => {
                        if (err) {
                            functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                        }

                        query = "insert into voice_talk values(?, ?, ?)";
                        db_api.query(query, [voice_talk_code, data.my_user_id, socket.id], (err) => {
                            if (err) {
                                functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                            }

                            VoiceTalkFCM(data.my_user_id, data.friend_user_id, data.my_nickname, chatroom_id);
                        });
                    });
                });
            }
        });
    });

    socket.on('voice_talk_answer', function(data) {
console.log('voice_talk_answer', data.my_user_id, data.friend_user_id, data.answer_type);
        const now_time = functions.GetTime();

        query = "select * from voice_talk where user_id = ?";
        db_api.query(query, [data.friend_user_id], (err, result) => {
            if (err) {
                functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                return;
            }

            if (result.length === 0) {
                return;
            }

            query = "select socket_id from voice_talk where voice_talk_code = ?";
            db_api.query(query, [result[0].voice_talk_code], (err, result) => {
                if (err) {
                    functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                    return;
                }

                result.forEach((row) => {
                    console.log('voice_talk_answer -> socket', row.socket_id, data.answer_type);
                    io.to(row.socket_id).emit('voice_talk_answer', { answer_type: data.answer_type });
                });
            });
        });
    });

    socket.on('enter_call', function(data) {
console.log('enter_call', data.my_user_id, data.friend_user_id);

	query = "delete from voice_talk where user_id = ?";
	db_api.query(query, [data.my_user_id], (err) => {
	    if (err) {
	        functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
	    }

	    query = "select * from voice_talk where user_id = ?";
            db_api.query(query, [data.friend_user_id], (err, result) => {
                if (err) {
                    functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                    return;
                }

                if (result.length === 0) {
                    return;
                }

                query = "insert into voice_talk values(?, ?, ?)";
                db_api.query(query, [result[0].voice_talk_code, data.my_user_id, socket.id]);
            });
        });
    });

    socket.on('sdp_offer', function(data) {
console.log('sdp_offer', data.my_user_id, data.friend_user_id);

        query = "select * from voice_talk where user_id = ?";
        db_api.query(query, [data.friend_user_id], (err, result) => {
            if (err) {
                functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                return;
            }

            io.to(result[0].socket_id).emit('sdp_offer', { sdp: data.sdp });
        });
    });

    socket.on('sdp_answer', function(data) {
console.log('sdp_answer', data.my_user_id, data.friend_user_id);

        query = "select * from voice_talk where user_id = ?";
        db_api.query(query, [data.friend_user_id], (err, result) => {
            if (err) {
                functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                return;
            }

            io.to(result[0].socket_id).emit('sdp_answer', { sdp: data.sdp });
        });
    });

    socket.on('ice_candidate', function(data) {
console.log('ice_candidate', data.my_user_id, data.friend_user_id);

        query = "select * from voice_talk where user_id = ?";
        db_api.query(query, [data.friend_user_id], (err, result) => {
            if (err) {
                functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                return;
            }

            io.to(result[0].socket_id).emit('ice_candidate', { sdp_mid: data.sdp_mid, sdp_m_line_index: data.sdp_m_line_index, sdp: data.sdp });
        });
    });


    function VoiceTalkFCM(my_user_id, friend_user_id, my_nickname, chatroom_id) {
        const now_time = functions.GetTime();
console.log('VoiceTalkFCM', my_user_id, friend_user_id, my_nickname, chatroom_id);

        let query = "select fcm_token from users where user_id = ?";
        db_api.query(query, [friend_user_id], (err, result) => {
            if (err) {
                functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                return;
            }

            if (result.length === 0) {
                console.log('No FCM token found for user:', friend_user_id);
                return;
            }

            const fcm_token = result[0].fcm_token;

            if (!fcm_token) {
                console.log('No FCM token available for user:', friend_user_id);
                return;
            }

            const message_info = {
                data: {
                    title: 'Voice Talk Invitation',
                    sender: String(my_nickname),
                    nickname: String(my_nickname),
                    body: `보이스톡 해요.`,
                    update_time: String(now_time),
                    chatroom_id: String(chatroom_id),
                    user_id: String(my_user_id),
                    notification_type: String(1),
                    msg_type: String(10),
                    chatroom_type: String(1)
                },
                token: fcm_token
            };

            firebase_admin.messaging().send(message_info)
                .then((response) => {
                    console.log('Successfully sent voice talk notification:', fcm_token);
                })
                .catch((error) => {
                    console.log('Error sending voice talk notification:', error);
                });
        });
    }
};
