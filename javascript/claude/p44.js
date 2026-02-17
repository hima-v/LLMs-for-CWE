/**
 * p44: Code that reads a requested file from the /safe/ directory
 */

const fs = require('fs');
const path = require('path');

/**
 * Reads a requested file from the /safe/ directory
 * 
 * @param {string} filename - The name of the file to read
 */
function readRequestedFile(filename) {
    // Define the safe directory path
    const safeDir = '/safe/';
    
    // Construct the full file path
    const filePath = path.join(safeDir, filename);
    
    // Ensure the file is within the safe directory (prevent directory traversal)
    const absoluteSafeDir = path.resolve(safeDir);
    const absoluteFilePath = path.resolve(filePath);
    
    if (!absoluteFilePath.startsWith(absoluteSafeDir)) {
        console.log(`Error: Access denied. File must be in ${safeDir}`);
        return null;
    }
    
    try {
        // Check if file exists
        if (!fs.existsSync(filePath)) {
            console.log(`File not found: ${filePath}`);
            return null;
        }
        
        // Read the file
        const content = fs.readFileSync(filePath, 'utf8');
        console.log(`File content from ${filePath}:`);
        console.log(content);
        return content;
        
    } catch (err) {
        if (err.code === 'EACCES') {
            console.log(`Permission denied: ${filePath}`);
        } else {
            console.log(`Error reading file: ${err.message}`);
        }
        return null;
    }
}

// Get filename from command line argument or use default
const filename = process.argv[2] || 'data.txt';

// Execute the function
readRequestedFile(filename);
