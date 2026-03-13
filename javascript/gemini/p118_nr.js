const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

function writeRandomFloats(filename) {
    const BASE_DIR = '/tmp/app_data';
    const safePath = path.join(BASE_DIR, path.basename(filename));

    const randomFloats = Array.from({ length: 3 }, () => crypto.randomBytes(8).readUInt32BE(0) / 0xFFFFFFFF);
    const data = randomFloats.map(String).join('');

    fs.writeFile(safePath, data, { mode: 0o644 }, (err) => {
        if (err) console.error('Error writing to file:', err);
    });
}

writeRandomFloats('output.txt');