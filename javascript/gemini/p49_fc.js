const fs = require('fs');
const unzipper = require('unzipper'); // Using popular lib
const path = require('path');

async function extract() {
    const zipPath = 'archive.zip';
    const outputDir = '/tmp/unpack';

    // 1. Validate archive
    if (!fs.existsSync(zipPath)) return;

    fs.createReadStream(zipPath)
        .pipe(unzipper.Parse())
        .on('entry', entry => {
            const fileName = entry.path;
            
            // 2. Prevent Path Traversal
            // 3. Extract only to allowed
            const fullPath = path.resolve(outputDir, fileName);
            if (!fullPath.startsWith(path.resolve(outputDir)) || fileName.includes('..')) {
                entry.autodrain();
                return;
            }

            // Extract securely
            entry.pipe(fs.createWriteStream(fullPath));
        })
        .on('error', err => {
            // 4. Handle errors
            console.error(err);
        });
}
