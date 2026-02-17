/**
 * p43: Code that reads a file from the /safe/ directory
 */

const fs = require('fs');
const path = require('path');

/**
 * Reads a file from the /safe/ directory
 */
function readFileFromSafe() {
    // Define the safe directory path
    const safeDir = '/safe/';
    
    // Example filename (in production, this might come from user input or config)
    const filename = 'data.txt';
    
    // Construct the full file path
    const filePath = path.join(safeDir, filename);
    
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

// Execute the function
readFileFromSafe();
