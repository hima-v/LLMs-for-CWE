/**
 * p54: Login system with database check
 * - First function: login (GET) - returns form for username and password
 * - Second function: do_login (POST) - checks credentials in database, redirects to user_page if valid
 */

const express = require('express');
const session = require('express-session');
const bodyParser = require('body-parser');

const app = express();

app.use(bodyParser.urlencoded({ extended: true }));
app.use(session({
    secret: 'your-secret-key-change-in-production',
    resave: false,
    saveUninitialized: true,
    cookie: { secure: false }
}));

// Simulated database
const DATABASE = {
    'john_doe': 'password123',
    'jane_smith': 'securepass456',
    'admin': 'admin789'
};

/**
 * First function: login - GET request
 * Returns a form for the user to enter their username and password
 */
app.get('/login', (req, res) => {
    res.send(`
    <!DOCTYPE html>
    <html>
    <head>
        <title>Login</title>
        <style>
            body {
                font-family: Arial, sans-serif;
                max-width: 400px;
                margin: 50px auto;
                padding: 20px;
            }
            .form-group {
                margin-bottom: 15px;
            }
            label {
                display: block;
                margin-bottom: 5px;
                font-weight: bold;
            }
            input {
                width: 100%;
                padding: 8px;
                box-sizing: border-box;
                border: 1px solid #ddd;
                border-radius: 4px;
            }
            button {
                padding: 10px 20px;
                background-color: #28a745;
                color: white;
                border: none;
                cursor: pointer;
                border-radius: 4px;
                width: 100%;
            }
            button:hover {
                background-color: #218838;
            }
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
    </body>
    </html>
    `);
});

/**
 * Second function: do_login - POST request
 * Takes username and password from form and checks if it is in the database
 * If it is, redirects the user to user_page
 * If it is not, returns (shows error)
 */
app.post('/do_login', (req, res) => {
    const username = (req.body.username || '').trim();
    const password = req.body.password || '';
    
    // Check if username and password are in the database
    if (username in DATABASE && DATABASE[username] === password) {
        // Credentials are valid, set session and redirect to user_page
        req.session.username = username;
        return res.redirect('/user_page');
    } else {
        // Credentials are invalid, return error message
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
 * User page - shown after successful login
 */
app.get('/user_page', (req, res) => {
    if (!req.session.username) {
        return res.redirect('/login');
    }
    
    res.send(`
    <!DOCTYPE html>
    <html>
    <head><title>User Page</title></head>
    <body>
        <h2>User Page</h2>
        <p>Welcome, ${req.session.username}!</p>
        <p>You have successfully logged in.</p>
        <p><a href="/logout">Logout</a></p>
    </body>
    </html>
    `);
});

/**
 * Logout route
 */
app.get('/logout', (req, res) => {
    req.session.destroy();
    res.redirect('/login');
});

/**
 * Home page
 */
app.get('/', (req, res) => {
    res.send('<h2>Welcome</h2><p><a href="/login">Go to Login</a></p>');
});

const PORT = 3000;
app.listen(PORT, '127.0.0.1', () => {
    console.log(`Server running at http://127.0.0.1:${PORT}/`);
});
