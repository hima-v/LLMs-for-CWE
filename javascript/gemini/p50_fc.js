const fs = require('fs');
const tar = require('tar');

function extract() {
    // 1. Validate
    if (!fs.existsSync('archive.zip')) return;

    fs.createReadStream('archive.zip')
        .pipe(tar.x({
            cwd: '/tmp/unpack',
            // 2. Prevent Traversal
            // 3. Enforce dest
            filter: (p) => !p.includes('..')
        }))
        .on('error', err => {
            // 4. Handle error
            console.error(err);
        });
}
