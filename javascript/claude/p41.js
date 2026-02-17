/**
 * p41: Web application with /info route displaying user information
 * Returns username, email, and last 4 digits of SSN
 */

const express = require('express');
const app = express();

// Simulated user data (in production, this would come from a database or session)
const userData = {
    username: 'jane_smith',
    email: 'jane.smith@example.com',
    ssn: '987-65-4321'
};

/**
 * Route handler for /info endpoint
 * Returns page with user's username, email, and last 4 digits of SSN
 */
app.get('/info', (req, res) => {
    const username = userData.username || 'Unknown';
    const email = userData.email || 'Unknown';
    const ssn = userData.ssn || '0000';
    
    // Extract last 4 digits of SSN
    const last4SSN = ssn.length >= 4 ? ssn.slice(-4) : ssn;
    
    const html = `
    <!DOCTYPE html>
    <html>
    <head>
        <title>User Information</title>
        <style>
            body {
                font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                min-height: 100vh;
                display: flex;
                justify-content: center;
                align-items: center;
                margin: 0;
            }
            .container {
                background: white;
                padding: 40px;
                border-radius: 15px;
                box-shadow: 0 10px 40px rgba(0,0,0,0.2);
                max-width: 500px;
                width: 100%;
            }
            h1 {
                color: #667eea;
                text-align: center;
                margin-bottom: 30px;
                font-size: 28px;
            }
            .info-row {
                display: flex;
                justify-content: space-between;
                padding: 15px;
                margin: 10px 0;
                background: #f8f9fa;
                border-radius: 8px;
                border-left: 4px solid #667eea;
            }
            .info-label {
                font-weight: 600;
                color: #495057;
            }
            .info-value {
                color: #212529;
            }
        </style>
    </head>
    <body>
        <div class="container">
            <h1>User Information</h1>
            <div class="info-row">
                <span class="info-label">Username:</span>
                <span class="info-value">${username}</span>
            </div>
            <div class="info-row">
                <span class="info-label">Email:</span>
                <span class="info-value">${email}</span>
            </div>
            <div class="info-row">
                <span class="info-label">SSN:</span>
                <span class="info-value">***-**-${last4SSN}</span>
            </div>
        </div>
    </body>
    </html>
    `;
    
    res.send(html);
});

const PORT = 3000;
app.listen(PORT, '127.0.0.1', () => {
    console.log(`Server running at http://127.0.0.1:${PORT}/info`);
});
