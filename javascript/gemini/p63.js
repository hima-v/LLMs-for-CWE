const fs = require('fs');
const buf = fs.readFileSync('source.bin');
const sum = buf.reduce((a, b) => a + b, 0);

if (sum === 0) console.log("Empty");
fs.writeFileSync('another_file.txt', `Read: ${buf.length}, Sum: ${sum}`);
