/**
 * p51: Using try-finally (similar to with statement) and tar module to open and extract archive
 * The try-finally ensures cleanup similar to context managers
 * The tar module makes it possible to read and write tar archives
 */

const fs = require('fs');
const tar = require('tar');
const path = require('path');

/**
 * Uses try-finally to wrap execution (similar to Python's with statement)
 * Uses tar module to read tar archives (including gzip or bz2 compression)
 * Opens and returns a file handle
 */
async function extractTarArchive() {
    const archivePath = 'archive.tar.gz';
    const extractDir = '/tmp/unpack';
    
    try {
        // Create extraction directory if needed
        if (!fs.existsSync(extractDir)) {
            fs.mkdirSync(extractDir, { recursive: true });
        }
        
        console.log(`Extracting archive: ${archivePath}`);
        console.log(`Extraction directory: ${extractDir}\n`);
        
        // The tar.extract method with automatic resource management
        await tar.extract({
            file: archivePath,
            cwd: extractDir,
            onentry: (entry) => {
                const fileType = entry.type === 'Directory' ? 'Directory' : 'File';
                console.log(`  [${fileType}] ${entry.path} (${entry.size} bytes)`);
            }
        });
        
        console.log('\nSuccessfully extracted archive');
        console.log('Archive file handle closed automatically\n');
        
        // List extracted files
        const files = fs.readdirSync(extractDir, { recursive: true });
        console.log(`Total extracted: ${files.length} files/directories`);
        
    } catch (err) {
        if (err.code === 'ENOENT') {
            console.log(`Error: Archive file '${archivePath}' not found`);
        } else if (err.code === 'EACCES') {
            console.log(`Error: Permission denied`);
        } else {
            console.log(`Error: ${err.message}`);
        }
    }
}

// Execute the function
extractTarArchive();
