/**
 * p40: Web application with /info route displaying user information from session
 * Returns username, email, and last 4 digits of SSN stored in session
 */

const express = require('express');
const session = require('express-session');

const app = express();

// Session configuration
app.use(session({
    secret: 'your-secret-key-here-change-in-production',
    resave: false,
    saveUninitialized: true,
    cookie: { secure: false } // Set to true in production with HTTPS
}));

/**
 * Route handler for /info endpoint
 * Retrieves user data from session and displays it
 */
app.get('/info', (req, res) => {
    const username = req.session.username || 'N/A';
    const email = req.session.email || 'N/A';
    const ssn = req.session.ssn || 'N/A';
    
    // Extract last 4 digits of SSN
    const last4SSN = ssn.length >= 4 ? ssn.slice(-4) : ssn;
    
    const html = `
    <!DOCTYPE html>
    <html>
    <head>
        <title>User Information</title>
        <style>
            body {
                font-family: Arial, sans-serif;
                max-width: 600px;
                margin: 50px auto;
                padding: 20px;
                background-color: #f5f5f5;
            }
            .info-card {
                background: white;
                padding: 30px;
                border-radius: 8px;
                box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            }
            h2 {
                color: #333;
                border-bottom: 2px solid #007bff;
                padding-bottom: 10px;
            }
            .info-item {
                margin: 15px 0;
                padding: 10px;
                background-color: #f8f9fa;
                border-left: 3px solid #007bff;
            }
            .label {
                font-weight: bold;
                color: #555;
            }
            .value {
                color: #333;
                margin-left: 10px;
            }
        </style>
    </head>
    <body>
        <div class="info-card">
            <h2>User Information</h2>
            <div class="info-item">
                <span class="label">Username:</span>
                <span class="value">${username}</span>
            </div>
            <div class="info-item">
                <span class="label">Email:</span>
                <span class="value">${email}</span>
            </div>
            <div class="info-item">
                <span class="label">SSN (Last 4 digits):</span>
                <span class="value">***-**-${last4SSN}</span>
            </div>
        </div>
    </body>
    </html>
    `;
    
    res.send(html);
});

/**
 * Helper route to set session data for testing
 */
app.get('/set_session', (req, res) => {
    req.session.username = 'john_doe';
    req.session.email = 'john.doe@example.com';
    req.session.ssn = '123-45-6789';
    res.send('Session data set. Visit <a href="/info">/info</a> to view.');
});

const PORT = 3000;
app.listen(PORT, '127.0.0.1', () => {
    console.log(`Server running at http://127.0.0.1:${PORT}/info`);
});
