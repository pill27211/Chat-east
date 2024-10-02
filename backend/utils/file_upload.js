require('dotenv').config();

const express = require('express');
const multer = require('multer');
const path = require('path');
const fs = require('fs');

module.exports = function(io, db_api, functions, firebase_admin) {
    const app = express();

    const upload_dir = path.join(__dirname, '../upload_files');
    if (!fs.existsSync(upload_dir)) {
        fs.mkdirSync(upload_dir, { recursive: true });
    }

    app.use('/files', express.static(upload_dir));

    const storage = multer.diskStorage({
        destination: function (req, file, cb) {
            cb(null, upload_dir);
        },
        filename: function (req, file, cb) {
            cb(null, Date.now() + path.extname(file.originalname));
        }
    });

    const upload = multer({ storage: storage });

    app.post('/upload', upload.single('file'), (req, res) => {
        try {
            const file = req.file;
            console.log('파일 정보:', file);

            const user_id = req.body.user_id;
            const chatroom_id = req.body.chatroom_id;
            const message_type = req.body.message_type;
            const nickname = req.body.nickname;
            const chatroom_name = req.body.chatroom_name;
            const path_type = req.body.path_type;
            const real_file_name = req.body.real_file_name;

            console.log('User ID:', user_id);
            console.log('Chatroom ID:', chatroom_id);
            console.log('Message Type:', message_type);
            console.log('Nickname:', nickname);
            console.log('Chatroom Name:', chatroom_name);
            console.log('Path Type:', path_type);
            console.log('Real File Name:', real_file_name);

            const file_url = `${req.protocol}://${req.get('host')}/files/${file.filename}`;
            const now_time = functions.GetTime();

            let query = "insert into messages(user_id, nickname, chatroom_id, content, message_type, create_time, update_time, sub_content, content_size) values(?, ?, ?, ?, ?, ?, ?, ?, ?)";
            db_api.query(query, [user_id, nickname, chatroom_id, file_url, message_type, now_time, now_time, real_file_name, file.size], (err, result) => {
                if (err) {
                    functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                    return res.status(500).send('Error inserting message.');
                }

                const message_id = result.insertId;

                query = "update chatrooms set updated_time = ?, last_message_id = ? where chatroom_id = ?";
                db_api.query(query, [now_time, message_id, chatroom_id]);

                var message = {
                    user_id: user_id,
                    nickname: nickname,
                    chatroom_id: chatroom_id,
                    content: file_url,
                    message_type: message_type,
                    create_time: now_time,
                    update_time: now_time,
                    message_id: message_id,
                    width: 0,
                    height: 0,
                    file_name: real_file_name,
                    file_size: file.size
                };

                query = "select socket_id from test where chatroom_id = ?";
                db_api.query(query, [chatroom_id], (err, sockets) => {
                    if (err) {
                        functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                        return;
                    }

                    sockets.forEach(socket_info => {
                        const socket_id = socket_info.socket_id;
                        io.to(socket_id).emit('msg_to_client', message);
                    });

                    query = "select u.user_id, u.nickname, u.fcm_token, uc.notification_type, c.chatroom_type from users u join userchatrooms uc on u.user_id = uc.user_id join chatrooms c on uc.chatroom_id = c.chatroom_id where uc.chatroom_id = ? and uc.join_type = 1";
                    db_api.query(query, [chatroom_id], (err, tokens) => {
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
                                    title: String(chatroom_name),
                                    sender: String(nickname),
                                    body: String(req.file.filename),
                                    chatroom_id: String(chatroom_id),
                                    update_time: String(now_time),
                                    notification_type: String(notification_type),
                                    user_id: String(user_id),
                                    nickname: String(nickname),
                                    chatroom_type: String(chatroom_type),
                                    msg_type: String(message_type)
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
                            download_url: file_url
                        });
                    });
                });
            });
        } catch (error) {
            console.error('Upload error:', error);
            res.status(500).send('File upload failed');
        }
    });

    app.listen(process.env.FILE_UPLOAD_PORT, () => {
        console.log("listening on *:" + process.env.FILE_UPLOAD_PORT);
    });
};


