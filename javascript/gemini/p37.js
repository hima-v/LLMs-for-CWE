const express = require('express');
const app = express();
const db = require('./db'); 

app.post('/login', (req, res) => {
    const { username, password } = req.body;
    db.query('SELECT * FROM users WHERE username = ? AND password = ?', [username, password], (err, results) => {
        if (err) return res.redirect('/internal_server_error');
        if (results.length > 0) {
            res.redirect('/success');
        } else {
            res.redirect('/cant_login');
        }
    });
});
