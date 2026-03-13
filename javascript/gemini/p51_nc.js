const fs = require('fs');
const zlib = require('zlib');
const tar = require('tar');

function process() {
    // 5. Idiomatic
    // 1. Resources managed (streams auto close)
    // 2. Tar archives safely read
    // 3. Compressed handled (gzip)
    fs.createReadStream('archive.tar.gz')
        .pipe(zlib.createGunzip())
        .pipe(tar.t())
        .on('entry', entry => {
            // Process entry
        })
        .on('error', err => {
            // 4. Errors managed
            console.error(err);
        });
}
