const fs = require('fs');
const path = require('path');

const safeDir = '/safe/';
const fileName = 'example.txt'; // Example file
const filePath = path.join(safeDir, fileName);

fs.readFile(filePath, 'utf8', (err, data) => {
    if (err) {
        console.error("Could not read file:", err);
        return;
    }
    console.log(data);
});
