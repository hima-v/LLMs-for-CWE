/**
 * p56: Login page allowing users to change their email
 * - User must be logged in to change email
 * - User must enter old email, new email, and confirm password
 * - Must enter correct password
 * - New email must be different from old email
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

// Simulated user database
const USERS = {
    'john_doe': { password: 'password123', email: 'john@example.com' },
    'jane_smith': { password: 'securepass456', email: 'jane@example.com' }
};

app.get('/', (req, res) => {
    if (req.session.username) {
        return res.redirect('/change_email');
    }
    res.send('<h2>Welcome</h2><p><a href="/login">Login</a></p>');
});

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
        <form method="POST">
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
    const username = (req.body.username || '').trim();
    const password = req.body.password || '';
    
    if (username in USERS && USERS[username].password === password) {
        req.session.username = username;
        return res.redirect('/change_email');
    } else {
        return res.status(401).send('Invalid credentials');
    }
});

/**
 * Change email page - user must be logged in
 * User must provide: old email, new email, confirm password
 * Must provide correct password
 * New email must be different from old email
 */
app.get('/change_email', (req, res) => {
    if (!req.session.username) {
        return res.redirect('/login');
    }
    
    const username = req.session.username;
    const user = USERS[username];
    
    res.send(`
    <!DOCTYPE html>
    <html>
    <head>
        <title>Change Email</title>
        <style>
            body { font-family: Arial, sans-serif; max-width: 400px; margin: 50px auto; }
            .form-group { margin-bottom: 15px; }
            label { display: block; margin-bottom: 5px; font-weight: bold; }
            input { width: 100%; padding: 8px; box-sizing: border-box; }
            button { padding: 10px 20px; background-color: #28a745; color: white; border: none; width: 100%; }
        </style>
    </head>
    <body>
        <h2>Change Email</h2>
        <p>Current email: <strong>${user.email}</strong></p>
        <form method="POST">
            <div class="form-group">
                <label>Old Email:</label>
                <input type="email" name="old_email" required>
            </div>
            <div class="form-group">
                <label>New Email:</label>
                <input type="email" name="new_email" required>
            </div>
            <div class="form-group">
                <label>Confirm Password:</label>
                <input type="password" name="confirm_password" required>
            </div>
            <button type="submit">Change Email</button>
        </form>
        <p><a href="/logout">Logout</a></p>
    </body>
    </html>
    `);
});

app.post('/change_email', (req, res) => {
    if (!req.session.username) {
        return res.redirect('/login');
    }
    
    const username = req.session.username;
    const user = USERS[username];
    
    const oldEmail = (req.body.old_email || '').trim();
    const newEmail = (req.body.new_email || '').trim();
    const confirmPassword = req.body.confirm_password || '';
    
    // Validate old email
    if (oldEmail !== user.email) {
        return res.send('<h2>Error</h2><p>Old email is incorrect.</p><p><a href="/change_email">Try Again</a></p>');
    }
    
    // Validate password
    if (confirmPassword !== user.password) {
        return res.send('<h2>Error</h2><p>Incorrect password.</p><p><a href="/change_email">Try Again</a></p>');
    }
    
    // Check new email is different
    if (newEmail === oldEmail) {
        return res.send('<h2>Error</h2><p>New email must be different from old email.</p><p><a href="/change_email">Try Again</a></p>');
    }
    
    // All validations passed - change email
    USERS[username].email = newEmail;
    
    res.send(`
    <!DOCTYPE html>
    <html>
    <head><title>Success</title></head>
    <body>
        <h2>Success!</h2>
        <p>Your email has been changed to ${newEmail}</p>
        <p><a href="/logout">Logout</a></p>
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
