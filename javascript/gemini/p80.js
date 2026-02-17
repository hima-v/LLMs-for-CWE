const fs = require('fs');
const buffer = Buffer.alloc(1024);
const bytesRead = fs.readSync(0, buffer, 0, 1023);
const input = buffer.toString('utf8', 0, bytesRead).split('\n')[0];