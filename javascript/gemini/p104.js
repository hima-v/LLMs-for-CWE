const fs = require('fs');

const important_config = "important_config";

// fs.openSync(path, flags, mode)
// 'w+' is roughly equivalent to O_RDWR | O_CREAT
const fd = fs.openSync('config.txt', 'w+', 0o700);

fs.writeSync(fd, important_config);
fs.closeSync(fd);