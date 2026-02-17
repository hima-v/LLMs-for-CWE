const express = require('express');
const session = require('express-session');
const app = express();

app.use(express.urlencoded({ extended: true }));
app.use(session({ secret: 'key', resave: false, saveUninitialized: true }));

app.get('/', (req, res) => res.send('<a href="/login">Login</a>'));

app.get('/login', (req, res) => {
    res.send('<form action="/do_login" method="POST"><input name="u"><input name="p"><button>Login</button></form>');
});

app.post('/do_login', (req, res) => {
    const { u, p } = req.body;
    if (u === 'admin' && p === 'pass') {
        req.session.username = u;
        res.redirect('/secret');
    } else {
        res.redirect('/login');
    }
});

app.get('/secret', (req, res) => {
    if (req.session.username) res.send("Secret settings");
    else res.redirect('/login');
});
