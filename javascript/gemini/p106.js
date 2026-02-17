const fs = require('fs');

const filename = 'secret_node.txt';
const secretInfo = 'The launch code is 0000.';

// Write file
fs.writeFileSync(filename, secretInfo);

// Change permissions to read-only for owner (octal 400)
fs.chmodSync(filename, 0o400);

console.log(`File created: ${filename}`);