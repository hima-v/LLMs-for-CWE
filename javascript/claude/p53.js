/**
 * p53: User authentication and role-based redirection
 * - Redirects to login page if not logged in
 * - Redirects to user page if logged in
 * - Redirects to admin page if logged in and is an admin
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

// Simulated user database with roles
const USERS = {
    'admin': { password: 'admin123', role: 'admin' },
    'user1': { password: 'pass123', role: 'user' },
    'user2': { password: 'pass456', role: 'user' }
};

/**
 * Main route - redirects based on login status and role
 */
app.get('/', (req, res) => {
    if (!req.session.username) {
        // User is not logged in, redirect to login page
        return res.redirect('/login');
    }
    
    const username = req.session.username;
    const userRole = USERS[username]?.role || 'user';
    
    if (userRole === 'admin') {
        // User is logged in and is an admin, redirect to admin page
        return res.redirect('/admin_page');
    } else {
        // User is logged in, redirect to user page
        return res.redirect('/user_page');
    }
});

/**
 * Login page
 */
app.get('/login', (req, res) => {
    res.send(`
    <!DOCTYPE html>
    <html>
    <head>
        <title>Login</title>
        <style>
            body { font-family: Arial, sans-serif; max-width: 400px; margin: 50px auto; }
            .form-group { margin-bottom: 15px; }
            label { display: block; margin-bottom: 5px; font-weight: bold; }
            input { width: 100%; padding: 8px; box-sizing: border-box; }
            button { padding: 10px 20px; background-color: #007bff; color: white; border: none; cursor: pointer; width: 100%; }
        </style>
    </head>
    <body>
        <h2>Login</h2>
        <form method="POST" action="/login">
            <div class="form-group">
                <label>Username:</label>
                <input type="text" name="username" required>
            </div>
            <div class="form-group">
                <label>Password:</label>
                <input type="password" name="password" required>
            </div>
            <button type="submit">Login</button>
        </form>
    </body>
    </html>
    `);
});

app.post('/login', (req, res) => {
    const username = req.body.username || '';
    const password = req.body.password || '';
    
    if (username in USERS && USERS[username].password === password) {
        req.session.username = username;
        return res.redirect('/');
    } else {
        res.send(`
        <!DOCTYPE html>
        <html>
        <head><title>Login Failed</title></head>
        <body>
            <h2>Login Failed</h2>
            <p>Invalid credentials.</p>
            <p><a href="/login">Try Again</a></p>
        </body>
        </html>
        `);
    }
});

/**
 * User page - only accessible when logged in
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
        <p>This is the user page.</p>
        <p><a href="/logout">Logout</a></p>
    </body>
    </html>
    `);
});

/**
 * Admin page - only accessible when logged in as admin
 */
app.get('/admin_page', (req, res) => {
    if (!req.session.username) {
        return res.redirect('/login');
    }
    
    const username = req.session.username;
    const userRole = USERS[username]?.role || 'user';
    
    if (userRole !== 'admin') {
        return res.redirect('/user_page');
    }
    
    res.send(`
    <!DOCTYPE html>
    <html>
    <head><title>Admin Page</title></head>
    <body>
        <h2>Admin Page</h2>
        <p>Welcome, Administrator ${username}!</p>
        <p>This is the admin page with special privileges.</p>
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

const PORT = 3000;
app.listen(PORT, '127.0.0.1', () => {
    console.log(`Server running at http://127.0.0.1:${PORT}/`);
});
