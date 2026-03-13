const fs = require('fs');
const path = require('path');
const unzipper = require('unzipper'); // Assuming a common library like unzipper or adm-zip is available

async function extractArchive() {
    const zipFilePath = 'archive.zip';
    const destDir = '/tmp/unpack';

    // 1. Validate archive file exists
    if (!fs.existsSync(zipFilePath)) {
        console.error("Archive not found");
        return;
    }

    // Ensure destination exists
    if (!fs.existsSync(destDir)) {
        fs.mkdirSync(destDir, { recursive: true });
    }

    // Resolve canonical destination path for Zip Slip check
    const resolvedDest = path.resolve(destDir);

    fs.createReadStream(zipFilePath)
        .pipe(unzipper.Parse())
        .on('entry', function (entry) {
            const fileName = entry.path;
            const fullPath = path.join(destDir, fileName);
            const resolvedPath = path.resolve(fullPath);

            // 2. Security Check (Zip Slip)
            // Prevent overwriting system paths by ensuring resolved path starts with resolved destination
            if (!resolvedPath.startsWith(resolvedDest + path.sep)) {
                console.error("Skipping malicious path:", fileName);
                entry.autodrain();
                return;
            }

            if (entry.type === 'Directory') {
                fs.mkdirSync(fullPath, { recursive: true });
                entry.autodrain();
            } else {
                // Ensure directory exists
                fs.mkdirSync(path.dirname(fullPath), { recursive: true });
                entry.pipe(fs.createWriteStream(fullPath));
            }
        })
        .on('error', function (err) {
            // Handle errors gracefully
            console.error("Extraction error:", err.message);
        });
}

extractArchive();
