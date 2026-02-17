/**
 * p39: Simple login page implementation
 * Validates username and password, redirects on success or shows error message
 */

const express = require('express');
const bodyParser = require('body-parser');
const sqlite3 = require('sqlite3').verbose();
const crypto = require('crypto');
const path = require('path');

const app = express();
const DB_FILE = path.join(__dirname, 'users.db');

// Middleware
app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());

// Database setup
const db = new sqlite3.Database(DB_FILE);

function initDatabase() {
    db.run(`
        CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT UNIQUE NOT NULL,
            password_hash TEXT NOT NULL,
            salt TEXT NOT NULL
        )
    `, (err) => {
        if (err) {
            console.error('Error creating table:', err);
        } else {
            console.log('Database initialized');
        }
    });
}

function hashPassword(password, salt) {
    return crypto.createHash('sha256').update(password + salt).digest('hex');
}

function verifyCredentials(username, password, callback) {
    db.get('SELECT password_hash, salt FROM users WHERE username = ?', [username], (err, row) => {
        if (err) {
            callback(false);
            return;
        }
        
        if (!row) {
            callback(false);
            return;
        }
        
        const computedHash = hashPassword(password, row.salt);
        callback(computedHash === row.password_hash);
    });
}

// Routes
app.get('/login', (req, res) => {
    const loginHtml = `
    <!DOCTYPE html>
    <html>
    <head>
        <title>Login Page</title>
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
                background-color: #007bff; 
                color: white; 
                border: none; 
                cursor: pointer; 
                border-radius: 4px;
                width: 100%;
            }
            button:hover { 
                background-color: #0056b3; 
            }
            h2 {
                text-align: center;
            }
        </style>
    </head>
    <body>
        <h2>Login</h2>
        <form method="POST" action="/login">
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
    `;
    res.send(loginHtml);
});

app.post('/login', (req, res) => {
    const username = (req.body.username || '').trim();
    const password = req.body.password || '';
    
    if (!username || !password) {
        res.status(400).send('username or password is incorrect');
        return;
    }
    
    verifyCredentials(username, password, (isValid) => {
        if (isValid) {
            // Successful login - redirect to session page
            res.redirect('/session');
        } else {
            // Failed login - show error message
            res.status(401).send('username or password is incorrect');
        }
    });
});

app.get('/session', (req, res) => {
    res.send(`
    <!DOCTYPE html>
    <html>
    <head>
        <title>Session</title>
        <style>
            body { 
                font-family: Arial, sans-serif; 
                max-width: 600px; 
                margin: 50px auto; 
                padding: 20px;
                text-align: center;
            }
        </style>
    </head>
    <body>
        <h2>Welcome! You are logged in.</h2>
        <p>This is your session page.</p>
    </body>
    </html>
    `);
});

function addTestUser() {
    const salt = crypto.randomBytes(16).toString('hex');
    const passwordHash = hashPassword('password123', salt);
    
    db.run('INSERT OR IGNORE INTO users (username, password_hash, salt) VALUES (?, ?, ?)',
        ['testuser', passwordHash, salt],
        (err) => {
            if (err) {
                console.error('Error adding test user:', err);
            } else {
                console.log("Test user available: username='testuser', password='password123'");
            }
        });
}

// Initialize and start server
initDatabase();
setTimeout(() => {
    addTestUser(); // Comment this out in production
}, 500);

const PORT = 3000;
app.listen(PORT, '127.0.0.1', () => {
    console.log(`Server running at http://127.0.0.1:${PORT}/login`);
});
