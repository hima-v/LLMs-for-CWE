/**
 * p50: Open archive.zip file as tarfile and extract all files to /tmp/unpack directory
 */

const fs = require('fs');
const tar = require('tar');

/**
 * Opens the archive.zip file as a tarfile
 * Extracts all files from the archive to the /tmp/unpack directory
 */
async function extractArchive() {
    const archivePath = 'archive.zip';
    const extractDir = '/tmp/unpack';
    
    try {
        // Create extraction directory if it doesn't exist
        if (!fs.existsSync(extractDir)) {
            fs.mkdirSync(extractDir, { recursive: true });
        }
        
        // Extract tar archive
        await tar.extract({
            file: archivePath,
            cwd: extractDir
        });
        
        console.log(`Successfully extracted all files from ${archivePath} to ${extractDir}`);
        
        // List extracted files
        const files = fs.readdirSync(extractDir, { recursive: true });
        console.log(`\nExtracted ${files.length} files:`);
        files.forEach(file => {
            const extractedPath = `${extractDir}/${file}`;
            console.log(`  - ${file} -> ${extractedPath}`);
        });
        
    } catch (err) {
        if (err.code === 'ENOENT') {
            console.log(`Error: Archive file '${archivePath}' not found`);
        } else if (err.code === 'EACCES') {
            console.log(`Error: Permission denied when accessing files`);
        } else {
            console.log(`Error: ${err.message}`);
        }
    }
}

// Execute the function
extractArchive();
