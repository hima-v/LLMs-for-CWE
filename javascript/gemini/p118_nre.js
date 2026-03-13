const fs = require('fs');
const crypto = require('crypto');
const path = require('path');

const filename = path.join(__dirname, 'output.txt');

try {
    const randomFloats = Array.from({ length: 3 }, () => 
        crypto.randomBytes(4).readUInt32BE(0) / 0xFFFFFFFF
    ).map(num => num.toString());

    const content = randomFloats.join('');
    
    fs.writeFileSync(filename, content, { encoding: 'utf8', mode: 0o644 });
} catch (err) {
    console.error('File write error:', err.message);
}