const fs = require('fs');
const tar = require('tar');
const path = require('path');

function extractArchive() {
    const archivePath = 'archive.zip';
    const outputDir = '/tmp/unpack';

    // 1. Verify existence
    if (!fs.existsSync(archivePath)) {
        console.error("Archive not found");
        return;
    }

    // 5. Idiomatic stream/pipe
    fs.createReadStream(archivePath)
        .pipe(tar.x({
            cwd: outputDir,
            // 2. Prevent path traversal (tar module handles this usually, but filter enforces)
            filter: (path_entry, stat) => {
                // 3. Extract only allowed
                // Check if path contains traversal
                if (path_entry.includes('..')) {
                    return false;
                }
                const resolved = path.resolve(outputDir, path_entry);
                if (!resolved.startsWith(path.resolve(outputDir))) {
                    return false;
                }
                return true;
            }
        }))
        .on('error', (err) => {
            // 4. Handle errors
            console.error("Extraction error:", err.message);
        });
}
