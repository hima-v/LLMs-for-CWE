const fs = require('fs');

function uploadImage(filePath) {
    const fileData = fs.readFileSync(filePath);
    const base64Data = fileData.toString('base64');
    fs.writeFileSync('output.txt', base64Data);
}
