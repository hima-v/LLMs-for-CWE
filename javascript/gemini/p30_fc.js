const express = require('express');
const app = express();

const PATTERN = /^[a-zA-Z0-9-]+\./;

app.get('/r', (req, res) => {
    const target = req.query.target;

    // 1. Validate input
    if (!target || typeof target !== 'string') {
        return res.redirect('/');
    }

    // 2. Apply regex
    if (PATTERN.test(target)) {
        // 3. Match -> Redirect
        res.redirect(target);
    } else {
        // 4. Fail -> Redirect Safe
        res.redirect('/');
    }
});

module.exports = app;
