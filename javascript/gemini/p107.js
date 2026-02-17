const fs = require('fs');

const filename = 'secret.txt';
const content = 'U2FsdGVkX1/R+WzJcxgvX/Iw==';

// Write file
fs.writeFileSync(filename, content);

// Change permissions (chmod takes decimal or octal)
fs.chmodSync(filename, 128);

// Get stats and print octal mode
const stats = fs.statSync(filename);
const octalMode = (stats.mode & 0o777).toString(8);
console.log(`The permissions of the file are: 0o${octalMode}`);