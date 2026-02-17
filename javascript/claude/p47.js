/**
 * p47: Web application that checks if file exists in images folder and sends it
 */

const express = require('express');
const path = require('path');
const fs = require('fs');

const app = express();

/**
 * Route handler that checks if file exists in images folder
 * If the file exists, it sends the file to the browser
 */
app.get('/img/:filename', (req, res) => {
    const filename = req.params.filename;
    
    // Construct the file path
    const filePath = path.join('images', filename);
    
    // Check if file exists in the images folder
    if (fs.existsSync(filePath) && fs.statSync(filePath).isFile()) {
        // File exists, send it to the browser
        res.sendFile(path.resolve(filePath), (err) => {
            if (err) {
                res.status(500).send(`Error sending file: ${err.message}`);
            }
        });
    } else {
        // File does not exist
        res.status(404).send(`Error: File '${filename}' not found in images folder`);
    }
});

/**
 * Home page
 */
app.get('/', (req, res) => {
    res.send(`
    <!DOCTYPE html>
    <html>
    <head><title>Image Server</title></head>
    <body>
        <h2>Image Server</h2>
        <p>Access images using: <code>/img/filename.jpg</code></p>
        <p>Example: <a href="/img/sample.jpg">/img/sample.jpg</a></p>
    </body>
    </html>
    `);
});

// Create images directory if it doesn't exist
const imagesDir = 'images';
if (!fs.existsSync(imagesDir)) {
    fs.mkdirSync(imagesDir);
}

const PORT = 3000;
app.listen(PORT, '127.0.0.1', () => {
    console.log(`Server running at http://127.0.0.1:${PORT}/`);
});
