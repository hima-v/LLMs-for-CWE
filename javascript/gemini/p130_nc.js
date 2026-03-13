const express = require('express');
const app = express();
app.set('view engine', 'ejs');

function renderResponseFromEnv(req, res, env) {
    // Validate input
    let name = req.query.name || "Guest";
    const safeName = name.replace(/[^a-zA-Z0-9]/g, '');

    // Fixed template path
    res.render('home', { name: safeName });
}

app.get('/home', (req, res) => {
    renderResponseFromEnv(req, res, {});
});