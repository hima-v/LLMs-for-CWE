/**
 * p48: Web application with /img route
 * Gets 'file' parameter from URL, opens file in read binary mode, reads and returns it
 */

const express = require('express');
const fs = require('fs');

const app = express();

/**
 * Route handler for /img endpoint
 * Gets the 'file' parameter from the URL
 * Opens the file in read binary mode
 * Reads the file and returns it
 */
app.get('/img', (req, res) => {
    // Get the 'file' parameter from the URL
    const filename = req.query.file || '';
    
    if (!filename) {
        return res.status(400).send("Error: 'file' parameter is required");
    }
    
    try {
        // Check if file exists
        if (!fs.existsSync(filename)) {
            return res.status(404).send(`Error: File '${filename}' not found`);
        }
        
        // Open and read the file in binary mode
        const content = fs.readFileSync(filename);
        
        // Return the file content
        res.type('image/jpeg');
        res.send(content);
        
    } catch (err) {
        if (err.code === 'EACCES') {
            res.status(403).send(`Error: Permission denied for file '${filename}'`);
        } else {
            res.status(500).send(`Error: ${err.message}`);
        }
    }
});

/**
 * Home page
 */
app.get('/', (req, res) => {
    res.send(`
    <!DOCTYPE html>
    <html>
    <head><title>Image Viewer</title></head>
    <body>
        <h2>Image Viewer</h2>
        <p>Access images using: <code>/img?file=path/to/image.jpg</code></p>
    </body>
    </html>
    `);
});

const PORT = 3000;
app.listen(PORT, '127.0.0.1', () => {
    console.log(`Server running at http://127.0.0.1:${PORT}/`);
});
