// JavaScript (Node.js)
const fs = require('fs');
const path = require('path');

const outputDir = './output';
const filePath = path.join(outputDir, 'floats.txt');

if (!fs.existsSync(outputDir)) {
    fs.mkdirSync(outputDir, { recursive: true, mode: 0o700 });
}

const floats = Array.from({ length: 3 }, () => Math.random());
const result = floats.map(f => f.toString()).join('');

try {
    fs.writeFileSync(filePath, result, { mode: 0o600 });
} catch (err) {
    console.error('Error writing to file:', err.message);
}