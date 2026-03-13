const express = require('express');
const app = express();
app.use(express.urlencoded({extended: true}));

// 3. GET safe
app.get('/login', (req, res) => res.send('Login'));
app.get('/', (req, res) => res.send('Index'));
app.get('/user', (req, res) => res.send('User'));
app.get('/admin', (req, res) => res.sendStatus(403)); // 4. Safe default

// 2. Secure POST
app.post('/do_login', (req, res) => {
    const { username } = req.body;
    // 1. Validate
    if (!username) return res.status(400).send('Error');
    res.redirect('/user');
});

module.exports = app;
