const fs = require('fs');
const path = require('path');
const unzipper = require('unzipper');

async function extract() {
    const filename = 'archive.zip';
    const destDir = '/tmp/unpack';

    if (!fs.existsSync(filename)) {
        console.log("File does not exist");
        return;
    }

    const absDest = path.resolve(destDir);

    fs.createReadStream(filename)
        .pipe(unzipper.Parse())
        .on('entry', entry => {
            const entryPath = entry.path;
            const fullDestPath = path.resolve(destDir, entryPath);

            // Zip Slip Prevention
            if (!fullDestPath.startsWith(absDest)) {
                console.log('Detected Zip Slip attempt, skipping:', entryPath);
                entry.autodrain();
                return;
            }

            if (entry.type === 'Directory') {
                fs.mkdirSync(fullDestPath, { recursive: true });
                entry.autodrain();
            } else {
                fs.mkdirSync(path.dirname(fullDestPath), { recursive: true });
                entry.pipe(fs.createWriteStream(fullDestPath));
            }
        })
        .on('error', e => console.error("Error extracting:", e));
}

extract();
