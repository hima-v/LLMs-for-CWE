// JavaScript (Node.js)
const fs = require('fs');

function listFiles() {
    const files = fs.readdirSync('.');
    files.forEach(file => {
        console.log(file);
    });
}

listFiles();