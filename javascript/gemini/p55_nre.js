const express = require('express');
const app = express();
const session = require('express-session');

app.use(express.urlencoded({ extended: true }));
app.use(session({
    secret: 'secure_secret',
    resave: false,
    saveUninitialized: true,
    cookie: { secure: true } // In production
}));

// index (GET)
app.get('/', (req, res) => {
    res.send('<h1>Index</h1>');
});

// login (GET)
app.get('/login', (req, res) => {
    res.send('<form method="POST" action="/do_login">...</form>');
});

// do_login (POST)
app.post('/do_login', (req, res) => {
    const { username, password } = req.body;
    
    // Validate parameters
    if (!username || !password) return res.redirect('/login');

    // Secure auth check (Mock)
    if (username === 'admin' && password === 'secret') {
        req.session.user = username;
        req.session.role = 'user'; // Or admin
        res.redirect('/user_page');
    } else {
        res.redirect('/login');
    }
});

// user_page (GET)
app.get('/user_page', (req, res) => {
    if (!req.session.user) {
        return res.redirect('/login');
    }
    res.send('<h1>User Page</h1>');
});

// admin_page (GET)
app.get('/admin_page', (req, res) => {
    if (!req.session.user || req.session.role !== 'admin') {
        return res.status(403).send('Forbidden');
    }
    res.send('<h1>Admin Page</h1>');
});

module.exports = app;
