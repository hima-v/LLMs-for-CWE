const fs = require('fs');

// 'w' flag in Node corresponds to O_TRUNC | O_CREAT | O_WRONLY
fs.open('example.txt', 'w', (err, fd) => {
    if (err) throw err;
    
    fs.write(fd, 'Hello JS', (err) => {
        if (err) throw err;
        fs.close(fd, () => console.log('Done!'));
    });
});