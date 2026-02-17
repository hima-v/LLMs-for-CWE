/**
 * p49: Open archive.zip file as tarfile and unzip all files to /tmp/unpack directory
 */

const fs = require('fs');
const path = require('path');
const tar = require('tar');

/**
 * Opens the archive.zip file as a tarfile
 * Unzips all files from the archive to the /tmp/unpack directory
 */
async function unzipArchive() {
    const archivePath = 'archive.zip';
    const extractDir = '/tmp/unpack';
    
    try {
        // Create extraction directory if it doesn't exist
        if (!fs.existsSync(extractDir)) {
            fs.mkdirSync(extractDir, { recursive: true });
        }
        
        // Note: Typically .zip files aren't tar archives
        // For actual ZIP files, would use a zip library like 'adm-zip'
        // For TAR files (.tar, .tar.gz, .tar.bz2), use tar
        
        // Extract tar archive (assuming it's actually a tar file despite .zip extension)
        await tar.extract({
            file: archivePath,
            cwd: extractDir
        });
        
        console.log(`Successfully extracted all files from ${archivePath} to ${extractDir}`);
        
        // List extracted files
        const files = fs.readdirSync(extractDir, { recursive: true });
        console.log(`\nExtracted ${files.length} files:`);
        files.forEach(file => {
            console.log(`  - ${file}`);
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
unzipArchive();
