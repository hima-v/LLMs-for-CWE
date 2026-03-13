const fs = require('fs');

try {
    const files = fs.readdirSync('.');
    files.forEach(file => {
        console.log(file);
    });
} catch (err) {
    console.error(`Error reading directory: ${err.message}`);
}