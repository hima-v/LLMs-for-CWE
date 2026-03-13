const express = require('express');
const app = express();
app.use(express.urlencoded({extended: true}));

// 5. Idiomatic Express

// Index (GET)
app.get('/', (req, res) => {
    res.send('Index');
});

// Login (GET)
// 3. GET requests do not leak sensitive info
app.get('/login', (req, res) => {
    res.send('<form method="POST" action="/do_login">...</form>');
});

// Do_Login (POST)
app.post('/do_login', (req, res) => {
    const { username, password } = req.body;
    // 1. Validate all request parameters
    if (!username || !password) {
        // 4. Clear error handling
        return res.status(400).send("Missing credentials");
    }
    
    // 2. Handle POST securely
    if (username === 'user') {
        res.redirect('/user_page');
    } else {
        res.redirect('/login');
    }
});

app.get('/user_page', (req, res) => {
    res.send('User Page');
});

app.get('/admin_page', (req, res) => {
    // 4. Safe defaults
    res.status(403).send('Forbidden');
});

module.exports = app;
