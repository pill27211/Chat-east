module.exports = function(io, socket, db_api, functions) {
    socket.on('add_friend', function(data) {

        console.log('add', data.from_user_id, data.to_user_id);

        FriendCheck(data.from_user_id, data.to_user_id, (count) => {
            if (count === 0) {
                query = "insert into friends(from_user_id, to_user_id, friend_type, update_time) values(?, ?, 1, now())";
                db_api.query(query, [data.from_user_id, data.to_user_id], (err) => {
                    if (err) {
                        functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                        socket.emit('server_check', { server_check: "add_friend_null" });
                    }
                    else {
                        socket.emit('server_check', { server_check: "add_friend" });
                    }
                });
            }
            else {
                query = "update friends set friend_type = 1, update_time = now() where from_user_id = ? and to_user_id = ?";
                db_api.query(query, [data.from_user_id, data.to_user_id], (err) => {
                    if (err) {
                        functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                        socket.emit('server_check', { server_check: "add_friend_null" });
                    }
                    else {
                        socket.emit('server_check', { server_check: "add_friend" });
                    }
                });
            }

        });
    });

    socket.on('favorite_friend', function(data) {

        console.log('favorite', data.from_user_id, data.to_user_id);

        FriendCheck(data.from_user_id, data.to_user_id, (count) => {
            if (count === 1) {
                query = "update friends set friend_type = 2, update_time = now() where from_user_id = ? and to_user_id = ?";
                db_api.query(query, [data.from_user_id, data.to_user_id], (err) => {
                    if (err) {
                        functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                        socket.emit('server_check', { server_check: "favorite_friend_null" });
                    }
                    else {
                        socket.emit('server_check', { server_check: "favorite_friend" });
                    }
                });
            }
        });
    });

    socket.on('favorite_cancel', function(data) {

        console.log('favorite cancel', data.from_user_id, data.to_user_id);

        FriendCheck(data.from_user_id, data.to_user_id, (count) => {
            if (count === 1) {
                query = "update friends set friend_type = 1, update_time = now() where from_user_id = ? and to_user_id = ?";
                db_api.query(query, [data.from_user_id, data.to_user_id], (err) => {
                    if (err) {
                        functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                        socket.emit('server_check', { server_check: "favorite_cancel_null" });
                    }
                    else {
                        socket.emit('server_check', { server_check: "favorite_cancel" });
                    }
                });
            }
        });
    });



    socket.on('ignore_friend', function(data) {

        console.log('ignore', data.from_user_id, data.to_user_id);

        FriendCheck(data.from_user_id, data.to_user_id, (count) => {
            if (count === 1) {
                query = "update friends set friend_type = 3, update_time = now()  where from_user_id = ? and to_user_id = ?";
                db_api.query(query, [data.from_user_id, data.to_user_id], (err) => {
                    if (err) {
                        functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                        socket.emit('server_check', { server_check: "ignore_friend_null" });
                    }
                    else {
                        socket.emit('server_check', { server_check: "ignore_friend" });
                    }
                });
            }
            else {
                query = "insert into friends(from_user_id, to_user_id, friend_type, update_time) values(?, ?, 3, now())";
                db_api.query(query, [data.from_user_id, data.to_user_id], (err) => {
                    if (err) {
                        functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                        socket.emit('server_check', { server_check: "ignore_friend_null" });
                    }
                    else {
                        socket.emit('server_check', { server_check: "ignore_friend" });
                    }
                });
            }
        });
    });

    socket.on('ignore_cancel', function(data) {

        console.log('ignore cancel', data.from_user_id, data.to_user_id);

        FriendCheck(data.from_user_id, data.to_user_id, (count) => {
            if (count === 1) {
                query = "update friends set friend_type = 0, update_time = now()  where from_user_id = ? and to_user_id = ?";
                db_api.query(query, [data.from_user_id, data.to_user_id], (err) => {
                    if (err) {
                        functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                        socket.emit('server_check', { server_check: "ignore_cancel_null" });
                    }
                    else {
                        socket.emit('server_check', { server_check: "ignore_cancel" });
                    }
                });
            }
        });
    });

    socket.on('delete_friend', function(data) {

        console.log('delete', data.from_user_id, data.to_user_id);

        FriendCheck(data.from_user_id, data.to_user_id, (count) => {
            if (count === 1) {
                query = "update friends set friend_type = 0, update_time = now() where from_user_id = ? and to_user_id = ?";
                db_api.query(query, [data.from_user_id, data.to_user_id], (err) => {
                    if (err) {
                        functions.WriteErrorLog(`[${__filename}] - [${query}] : ${err.message}`);
                        socket.emit('server_check', { server_check: "delete_friend_null" });
                    }
                    else {
                        socket.emit('server_check', { server_check: "delete_friend" });
                    }
                });
            }
        });
    });


    function FriendCheck(from_user_id, to_user_id, callback) {
        let query = "select count(*) as count from friends where from_user_id = ? and to_user_id = ?";
        db_api.query(query, [from_user_id, to_user_id], (err, result) => {
            if (err) {
                functions.WriteErrorLog(`[${__filename}] - [${query}] : ${ err.message }`);
                callback(-1);
                return;
            }
            callback(result[0].count);
        });
    }
};