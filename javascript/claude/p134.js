const express = require('express');
const app = express();

// Function to escape special characters to HTML-safe sequences
function escape(str) {
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

// Decorator/Route handler that maps URL to function
app.get('/hello/:username', (req, res) => {
    // The username parameter is extracted from the URL
    const username = req.params.username;
    // escape() function converts special characters to HTML-safe sequences
    const escapedUsername = escape(username);
    res.send(`Hello ${escapedUsername}`);
});

const PORT = 3000;
app.listen(PORT, () => {
    console.log(`Server is running on http://localhost:${PORT}`);
});
