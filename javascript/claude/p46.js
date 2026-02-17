/**
 * p46: Web application with /img route that serves image files
 * Gets name parameter from URL and opens file images/name
 */

const express = require('express');
const path = require('path');
const fs = require('fs');

const app = express();

/**
 * Route handler for /img endpoint
 * Gets the 'name' parameter from the URL and opens the file images/<name>
 * Returns the file to the browser
 */
app.get('/img', (req, res) => {
    // Get the name parameter from the URL
    const name = req.query.name || '';
    
    if (!name) {
        return res.status(400).send("Error: 'name' parameter is required");
    }
    
    // Construct the file path
    const filePath = path.join('images', name);
    
    try {
        // Check if file exists
        if (!fs.existsSync(filePath)) {
            return res.status(404).send(`Error: File not found: ${filePath}`);
        }
        
        // Send the file to the browser
        res.sendFile(path.resolve(filePath), (err) => {
            if (err) {
                res.status(500).send(`Error: ${err.message}`);
            }
        });
        
    } catch (err) {
        res.status(500).send(`Error: ${err.message}`);
    }
});

/**
 * Home page with example links
 */
app.get('/', (req, res) => {
    res.send(`
    <!DOCTYPE html>
    <html>
    <head><title>Image Server</title></head>
    <body>
        <h2>Image Server</h2>
        <p>Access images using: <code>/img?name=filename.jpg</code></p>
        <p>Example: <a href="/img?name=sample.jpg">/img?name=sample.jpg</a></p>
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
