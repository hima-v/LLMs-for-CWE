const fs = require('fs');
const buf = Buffer.alloc(1024);
fs.readSync(0, buf, 0, 10);