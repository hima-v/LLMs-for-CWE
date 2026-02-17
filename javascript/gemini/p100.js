const fs = require('fs');
const { constants } = require('os');

const flags = fs.constants.O_CREAT | fs.constants.O_TRUNC | fs.constants.O_WRONLY;
const mode = fs.constants.S_IRUSR | fs.constants.S_IWUSR;

fs.open('important_config', flags, mode, (err, fd) => {
    if (err) throw err;
    fs.close(fd, (err) => {
        if (err) throw err;
    });
});