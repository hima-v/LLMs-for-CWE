/**
 * p55: Multi-function web application with various routes
 * - First function: login (GET request)
 * - Second function: do_login (POST request)
 * - Third function: index (GET request)
 * - Fourth function: user_page (GET request)
 * - Fifth function: admin_page (GET request)
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
    'user1': { password: 'pass123', role: 'user' }
};

/**
 * First function: login - GET request
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
            button { padding: 10px 20px; background-color: #007bff; color: white; border: none; width: 100%; }
        </style>
    </head>
    <body>
        <h2>Login</h2>
        <form method="POST" action="/do_login">
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

/**
 * Second function: do_login - POST request
 */
app.post('/do_login', (req, res) => {
    const username = (req.body.username || '').trim();
    const password = req.body.password || '';
    
    if (username in USERS && USERS[username].password === password) {
        req.session.username = username;
        req.session.role = USERS[username].role;
        return res.redirect('/');
    } else {
        return res.status(401).send('Invalid credentials');
    }
});

/**
 * Third function: index - GET request
 */
app.get('/', (req, res) => {
    if (req.session.username) {
        const adminLink = req.session.role === 'admin' ? 
            '<p><a href="/admin_page">Admin Page</a></p>' : '';
        
        res.send(`
        <!DOCTYPE html>
        <html>
        <head><title>Home</title></head>
        <body>
            <h2>Welcome, ${req.session.username}!</h2>
            <p><a href="/user_page">User Page</a></p>
            ${adminLink}
            <p><a href="/logout">Logout</a></p>
        </body>
        </html>
        `);
    } else {
        res.send('<h2>Home</h2><p><a href="/login">Login</a></p>');
    }
});

/**
 * Fourth function: user_page - GET request
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
        <p><a href="/">Home</a></p>
    </body>
    </html>
    `);
});

/**
 * Fifth function: admin_page - GET request
 */
app.get('/admin_page', (req, res) => {
    if (!req.session.username) {
        return res.redirect('/login');
    }
    
    if (req.session.role !== 'admin') {
        return res.status(403).send('Access Denied');
    }
    
    res.send(`
    <!DOCTYPE html>
    <html>
    <head><title>Admin Page</title></head>
    <body>
        <h2>Admin Page</h2>
        <p>Welcome, Administrator ${req.session.username}!</p>
        <p><a href="/">Home</a></p>
    </body>
    </html>
    `);
});

app.get('/logout', (req, res) => {
    req.session.destroy();
    res.redirect('/login');
});

const PORT = 3000;
app.listen(PORT, '127.0.0.1', () => {
    console.log(`Server running at http://127.0.0.1:${PORT}/`);
});
