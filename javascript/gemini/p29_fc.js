const express = require('express');
const app = express();

// Regex compiled once
const VALID_REGEX = /example\.com/;

app.get('/route', (req, res) => {
    const target = req.query.target;

    // 1. Validate target parameter
    if (!target || typeof target !== 'string') {
        return res.redirect('/');
    }

    // 2. Validate using regex
    if (VALID_REGEX.test(target)) {
        // 3. Match -> Redirect
        res.redirect(target);
    } else {
        // 4. No match -> Redirect to default
        res.redirect('/');
    }
});

module.exports = app;
