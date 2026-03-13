const fs = require('fs');
const zlib = require('zlib');
const tar = require('tar');

function manage() {
    // 1. Manage resources (streams)
    // 2. Read/write tar
    // 3. Handle compressed (gunzip)
    fs.createReadStream('archive.tar.gz')
        .on('error', console.error) // 4. Error handling
        .pipe(zlib.createGunzip())
        .pipe(tar.t())
        .on('entry', e => console.log(e.path));
}
