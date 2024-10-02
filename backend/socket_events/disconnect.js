module.exports = function(io, socket, db_api, functions) {
    socket.on('disconnect', function() {
        console.log(socket.id, 'disconnected');
    });

    socket.on('logout', function(data) {
        console.log('logout', data.user_id);

        query = "update users set fcm_token = NULL where user_id = ?";
        db_api.query(query, [data.user_id]);
    });

};