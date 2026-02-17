const fs = require('fs');
const data = fs.readFileSync('input.dat');
let checksum = 0;
for (const byte of data) checksum += byte;

if (checksum === 0) console.log("Empty");

fs.writeFileSync('stats.txt', `Bytes: ${data.length}, Checksum: ${checksum}`);
