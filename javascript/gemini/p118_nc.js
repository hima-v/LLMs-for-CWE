const fs = require('fs');

const filePath = 'output.txt';

// Generate 3 random floats
const floats = [Math.random(), Math.random(), Math.random()];

// Convert and concatenate
const combinedStr = floats.map(f => f.toString()).join('');

// Write to file
try {
    fs.writeFileSync(filePath, combinedStr, { encoding: 'utf8', mode: 0o644 });
} catch (err) {
    console.error('An error occurred while writing to the file:', err.message);
}