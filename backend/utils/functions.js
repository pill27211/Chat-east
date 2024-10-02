const moment = require('moment-timezone');
const path = require('path');
const fs = require('fs');

function GetTime(date) {
    if(date) {
        return moment(date).tz('Asia/Seoul').format('YYYY-MM-DD HH:mm:ss');
    }

    return moment().tz('Asia/Seoul').format('YYYY-MM-DD HH:mm:ss');
}

function WriteErrorLog(error_message) {
    const log_file_path = path.join(__dirname, '../error_log.txt');
    const write_message = `[${new Date().toLocaleString()}] - ${error_message}\n`;

    fs.appendFile(log_file_path, write_message, (err) => {
        if (err) {
            console.error('Error writing to error log :', err);
        }
    });
}

module.exports = {
    WriteErrorLog: WriteErrorLog,
    GetTime: GetTime
};
