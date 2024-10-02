module.exports = function(io, socket, db_api, functions) {
    socket.on('check_friend_update', function(data) {

        query = "select u.user_id, u.nickname, u.comment, f.friend_type, u.profile_img_url, u.background_img_url, greatest(u.last_profile_update, f.update_time) as last_profile_update from users u join friends f on u.user_id = f.to_user_id where f.from_user_id = ? and (u.last_profile_update > ? or f.update_time > ?)";
        db_api.query(query, [data.user_id, data.update_time, data.update_time], (err, result) => {
            if (err) {
                functions.WriteErrorLog(`[${__filename}] - [${query}] : ${ err.message }`);
                return;
            }

            if(result.length === 0) {
                socket.emit('friend_update', { friend_data: null });
                return;
            }

            result.forEach(friend_data => {
                friend_data.last_profile_update = functions.GetTime(friend_data.last_profile_update);
            });

            socket.emit('friend_update', { friend_data: result });
        });
    });

    socket.on('search_tag', function(data) {
        let query = "select user_id, nickname, comment, profile_img_url, background_img_url, last_profile_update from users where user_tag = ?";
        db_api.query(query, [data.user_tag], (err, result) => {
            if (err) {
                functions.WriteErrorLog(`[${__filename}] - [${query}] : ${ err.message }`);
                return;
            }

            if(result.length === 0) {
                socket.emit('search_tag', { friend_data: null });
                return;
            }

            let fdata = result[0];
            fdata.last_profile_update = functions.GetTime(fdata.last_profile_update);

            socket.emit('search_tag', { friend_data: [fdata] });

        });
    });
    socket.on('search_id', function(data) {
        console.log('search_id', data.user_id);
        let query = "select user_id, nickname, comment, profile_img_url, background_img_url, last_profile_update from users where user_id = ?";
        db_api.query(query, [data.user_id], (err, result) => {
            if (err) {
                functions.WriteErrorLog(`[${__filename}] - [${query}] : ${ err.message }`);
                return;
            }

            if(result.length === 0) {
                socket.emit('search_id', { user_data: null });
                return;
            }

            let udata = result[0];
            udata.last_profile_update = functions.GetTime(udata.last_profile_update);

            socket.emit('search_id', { user_data: [udata] });

        });
    });
    socket.on('get_image_url', function(data) {
        console.log('get_image_url', data.user_id);

        let query = "select profile_img_url, background_img_url from users where user_id = ?";
        db_api.query(query, [data.user_id], (err, result) => {
            if (err) {
                functions.WriteErrorLog(`[${__filename}] - [${query}] : ${ err.message }`);
                return;
            }

            const img_url = {
                profile_img_url: result[0].profile_img_url,
                background_img_url: result[0].background_img_url
            };

            socket.emit('get_image_url', { img_url });
        });
    });

    socket.on('get_profile', function(data) {
    console.log('get_profile', data.user_id, data.update_time);

        let query = "select * from users where user_id = ? and last_profile_update > ?";
        db_api.query(query, [data.user_id, data.update_time], (err, result) => {
            if (err) {
                functions.WriteErrorLog(`[${__filename}] - [${query}] : ${ err.message }`);
                return;
            }

            if(result.length === 0) {
                socket.emit('get_profile', { profile_data: null });
                return;
            }

            let pdata = result[0];
            pdata.last_profile_update = functions.GetTime(pdata.last_profile_update);

            socket.emit('get_profile', { profile_data: [pdata] });

        });
    });


};