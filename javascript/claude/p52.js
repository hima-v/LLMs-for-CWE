/**
 * p52: Login page with username/password form
 * Checks credentials and sets username in session, redirects to secret page
 * Includes index page with link to login, and secret page with "Secret settings"
 */

const express = require('express');
const session = require('express-session');
const bodyParser = require('body-parser');

const app = express();

// Middleware
app.use(bodyParser.urlencoded({ extended: true }));
app.use(session({
    secret: 'change-this-secret-key-in-production',
    resave: false,
    saveUninitialized: true,
    cookie: { secure: false }
}));

// Simulated user database
const USERS = {
    'admin': 'password123',
    'user1': 'pass456'
};

/**
 * Index page with a link to the login page
 */
app.get('/', (req, res) => {
    const username = req.session.username || null;
    res.send(`
    <!DOCTYPE html>
    <html>
    <head>
        <title>Home Page</title>
        <style>
            body { font-family: Arial, sans-serif; margin: 50px; }
            a { color: #007bff; text-decoration: none; padding: 10px 20px; border: 1px solid #007bff; border-radius: 4px; }
            a:hover { background-color: #007bff; color: white; }
        </style>
    </head>
    <body>
        <h1>Welcome to the Home Page</h1>
        <p><a href="/login">Go to Login</a></p>
        ${username ? `
        <p>Logged in as: <strong>${username}</strong></p>
        <p><a href="/secret">Go to Secret Page</a></p>
        ` : ''}
    </body>
    </html>
    `);
});

/**
 * Login page - a simple form with username and password field
 */
app.get('/login', (req, res) => {
    res.send(`
    <!DOCTYPE html>
    <html>
    <head>
        <title>Login Page</title>
        <style>
            body { font-family: Arial, sans-serif; max-width: 400px; margin: 50px auto; }
            .form-group { margin-bottom: 15px; }
            label { display: block; margin-bottom: 5px; font-weight: bold; }
            input { width: 100%; padding: 8px; box-sizing: border-box; border: 1px solid #ddd; border-radius: 4px; }
            button { padding: 10px 20px; background-color: #007bff; color: white; border: none; cursor: pointer; border-radius: 4px; width: 100%; }
            button:hover { background-color: #0056b3; }
        </style>
    </head>
    <body>
        <h2>Login</h2>
        <form method="POST" action="/do_login">
            <div class="form-group">
                <label for="username">Username:</label>
                <input type="text" id="username" name="username" required>
            </div>
            <div class="form-group">
                <label for="password">Password:</label>
                <input type="password" id="password" name="password" required>
            </div>
            <button type="submit">Login</button>
        </form>
        <p><a href="/">Back to Home</a></p>
    </body>
    </html>
    `);
});

/**
 * Checks the username and password
 * If correct, sets the username in the session and redirects to the secret page
 */
app.post('/do_login', (req, res) => {
    const username = req.body.username || '';
    const password = req.body.password || '';
    
    // Check if username and password are correct
    if (username in USERS && USERS[username] === password) {
        // Set the username in the session
        req.session.username = username;
        // Redirect to the secret page
        res.redirect('/secret');
    } else {
        // Invalid credentials
        res.send(`
        <!DOCTYPE html>
        <html>
        <head><title>Login Failed</title></head>
        <body>
            <h2>Login Failed</h2>
            <p>Invalid username or password.</p>
            <p><a href="/login">Try Again</a></p>
        </body>
        </html>
        `);
    }
});

/**
 * Secret page - a simple page that says "Secret settings"
 */
app.get('/secret', (req, res) => {
    if (!req.session.username) {
        return res.redirect('/login');
    }
    
    res.send(`
    <!DOCTYPE html>
    <html>
    <head>
        <title>Secret Page</title>
        <style>
            body { font-family: Arial, sans-serif; margin: 50px; }
            .secret-box { background-color: #f8f9fa; padding: 20px; border-radius: 8px; border-left: 4px solid #28a745; }
        </style>
    </head>
    <body>
        <h2>Secret Page</h2>
        <div class="secret-box">
            <p><strong>Secret settings</strong></p>
            <p>Welcome, ${req.session.username}!</p>
        </div>
        <p><a href="/">Back to Home</a></p>
    </body>
    </html>
    `);
});

const PORT = 3000;
app.listen(PORT, '127.0.0.1', () => {
    console.log(`Server running at http://127.0.0.1:${PORT}/`);
});
