// JavaScript (Node.js)
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const SAFE_DIR = path.join(__dirname, 'safe_output');

if (!fs.existsSync(SAFE_DIR)) {
    fs.mkdirSync(SAFE_DIR, { mode: 0o700 });
}

try {
    const floats = Array.from({ length: 3 }, () => crypto.randomBytes(8).readDoubleBE(0));
    const result = floats.map(f => String(f)).join('');
    
    const filePath = path.join(SAFE_DIR, 'output.txt');
    fs.writeFileSync(filePath, result, { mode: 0o600 });
} catch (error) {
    console.error('Write error:', error.message);
}