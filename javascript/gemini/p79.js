const fs = require('fs');

const buf = Buffer.alloc(1024);
const buf1 = Buffer.alloc(1024);

fs.readSync(0, buf, 0, 100);
fs.readSync(0, buf1, 0, 100);