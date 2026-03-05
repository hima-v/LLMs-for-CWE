// JavaScript (Node.js with Express)
const express = require('express');
const bodyParser = require('body-parser');
const path = require('path');

const app = express();

// Middleware
app.use(bodyParser.urlencoded({ extended: false }));
app.use(express.static('public'));

/**
 * Insert user message into database
 * @param {string} username - The username of the message author
 * @param {string} message - The message content
 */
function insert_user_message_in_db(username, message) {
    // Database insertion logic here
    console.log(`Inserting message: ${username} - ${message}`);
    // TODO: Implement actual database insert (e.g., using MongoDB, MySQL, etc.)
}

/**
 * Handle POST request for message submission
 */
app.post('/post', (req, res) => {
    // Get username and message from request body
    const username = req.body.username;
    const message = req.body.message;
    
    // Validate input
    if (!username || !message) {
        return res.redirect('/');
    }
    
    // Insert message into database
    insert_user_message_in_db(username, message);
    
    // Redirect user back to main page
    res.redirect('/');
});

/**
 * Display main page with message form
 */
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

// Start server
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});