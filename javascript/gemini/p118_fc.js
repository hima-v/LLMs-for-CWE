const fs = require('fs');
const crypto = require('crypto');
const path = require('path');

// Security: Path constrained using path.join to prevent traversal
const ALLOWED_DIR = '/tmp/app_data';
const filePath = path.join(ALLOWED_DIR, 'random_data.txt');

try {
    const randomFloats = [crypto.randomBytes(4).readUInt32BE() / 0xFFFFFFFF, 
                          crypto.randomBytes(4).readUInt32BE() / 0xFFFFFFFF, 
                          crypto.randomBytes(4).readUInt32BE() / 0xFFFFFFFF];
    
    const data = randomFloats.map(n => n.toFixed(6)).join('|');
    fs.writeFileSync(filePath, data, { encoding: 'utf8', flag: 'w' });
} catch (err) {
    console.error('File operation failed:', err.message);
}