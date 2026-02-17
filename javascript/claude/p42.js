/**
 * p42: Web application with /info route returning user information string
 * Returns string containing username, email, and SSN from session
 */

const express = require('express');
const session = require('express-session');

const app = express();

// Session configuration
app.use(session({
    secret: 'your-secret-key-change-in-production',
    resave: false,
    saveUninitialized: true,
    cookie: { secure: false }
}));

/**
 * Route handler for /info endpoint
 * Returns string with user's username, email, and SSN from session
 */
app.get('/info', (req, res) => {
    const username = req.session.username || 'N/A';
    const email = req.session.email || 'N/A';
    const ssn = req.session.ssn || 'N/A';
    
    // Return formatted string
    res.send(`Username: ${username}, Email: ${email}, SSN: ${ssn}`);
});

/**
 * Helper route to set session data for testing
 */
app.get('/set_session', (req, res) => {
    req.session.username = 'alice_johnson';
    req.session.email = 'alice.johnson@example.com';
    req.session.ssn = '555-44-3333';
    res.send('Session data set. Visit <a href="/info">/info</a> to view.');
});

const PORT = 3000;
app.listen(PORT, '127.0.0.1', () => {
    console.log(`Server running at http://127.0.0.1:${PORT}/info`);
});
