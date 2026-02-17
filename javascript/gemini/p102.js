const fs = require('fs');

const data = 'important_config';
// 0o644 corresponds to S_IRUSR | S_IWUSR | S_IRGRP | S_IROTH
const mode = 0o644; 

fs.open('important_config', 'w+', mode, (err, fd) => {
    if (err) throw err;
    fs.write(fd, data, (err) => {
        if (err) throw err;
        fs.close(fd, () => {
            console.log('File written successfully.');
        });
    });
});