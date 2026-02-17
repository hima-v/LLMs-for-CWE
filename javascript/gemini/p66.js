const fs = require('fs');
function savePdf(tempPath, originalName) {
    const target = `uploads/${originalName}`;
    fs.rename(tempPath, target, (err) => {
        if (err) throw err;
    });
}
