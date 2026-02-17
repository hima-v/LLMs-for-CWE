/**
 * p45: Code that reads the requested file from the safe directory
 */

const fs = require('fs');
const path = require('path');

/**
 * Reads the requested file from the safe directory
 * 
 * @param {string} filename - The name of the file to read from safe directory
 */
function readFileFromSafeDirectory(filename) {
    // Define the safe directory
    const safeDirectory = 'safe';
    
    // Construct the full file path
    const filePath = path.join(safeDirectory, filename);
    
    // Normalize paths to prevent directory traversal
    const safeDirAbs = path.resolve(safeDirectory);
    const filePathAbs = path.resolve(filePath);
    
    // Security check: ensure the file is within the safe directory
    if (!filePathAbs.startsWith(safeDirAbs)) {
        console.log(`Error: Access denied. File must be in ${safeDirectory} directory`);
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
        console.log(`Successfully read file: ${filePath}`);
        console.log('Content:');
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

// Get filename from command line or use default
const filename = process.argv[2] || 'example.txt';

// Execute the function
readFileFromSafeDirectory(filename);
