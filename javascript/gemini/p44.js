const fs = require('fs');
const path = require('path');

function readRequested(fileName) {
    const fullPath = path.join('/safe/', fileName);
    fs.readFile(fullPath, 'utf8', (err, data) => {
        if (err) console.log("Error");
        else console.log(data);
    });
}
