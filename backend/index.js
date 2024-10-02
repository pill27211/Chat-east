require('dotenv').config({ path: 'utils/.env' });

const firebase_admin = require('./utils/firebase');
const functions = require('./utils/functions');
const app = require("express")();
const http = require("http").createServer(app);
const io = require('socket.io')(http);
const mysql = require('mysql');
const fs = require('fs');

const db_api = mysql.createConnection({
    host: process.env.DB_HOST,
    port: process.env.DB_PORT,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME,
    charset: "utf8mb4"
});

db_api.connect(err => {
    if (err) {
        console.error('Error connecting to MySQL', err);
        return;
    }
    console.log('Connected to MySQL');
});

io.on('connection', function(socket) {
    console.log(socket.id, 'Connected');

    var id_message = {
        id: `socket_id : ${socket.id}`
    };
    socket.emit('check_con', id_message);

    require('./socket_events/login_success')(socket, db_api, functions);
    require('./socket_events/message')(io, socket, db_api, functions, firebase_admin, app, fs);
    require('./socket_events/disconnect')(io, socket, db_api, functions);
    require('./socket_events/chatroom')(io, socket, db_api, functions);
    require('./socket_events/profile')(io, socket, db_api, functions);
    require('./socket_events/friend')(io, socket, db_api, functions);
    require('./socket_events/setting')(io, socket, db_api, functions);
});

http.listen(process.env.APPLICATION_PORT, () => {
    console.log("listening on *:" + process.env.APPLICATION_PORT);
});

require('./utils/file_upload')(io, db_api, functions, firebase_admin);