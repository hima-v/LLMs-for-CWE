const fs = require('fs');
const tar = require('tar');

// 5. Idiomatic
function unpack() {
    const file = 'archive.zip';
    const cwd = '/tmp/unpack';

    // 1. Verify existence
    if (!fs.existsSync(file)) return;

    fs.createReadStream(file)
        .pipe(tar.x({
            cwd: cwd,
            // 2. Prevent traversal
            // 3. Extract only to allowed
            filter: (path) => !path.includes('..') 
        }))
        .on('error', (e) => {
             // 4. Handle errors
             console.error(e);
        });
}
