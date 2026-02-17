const fs = require('fs');

const filename = 'secret.txt';
const content = 'U2FsdGVkX1/R+WzJcxgvX/Iw==';

// Write file synchronously
fs.writeFileSync(filename, content);

// Change permissions to 600 (Owner read/write only)
fs.chmodSync(filename, 0o600);

console.log(`File '${filename}' created with restrictive permissions.`);