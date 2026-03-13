const express = require('express');
const app = express();

// 2. Ensure regex is compiled once
// Regex: Starts with 1+ alphanumeric/hyphen then a period
const REGEX = /^[a-zA-Z0-9-]+\./;

app.get('/redirect', (req, res) => {
    const target = req.query.target;

    // 1. Verify that the input is not null or undefined
    if (!target || typeof target !== 'string') {
        return res.redirect('/');
    }

    // 3. Confirm target strictly matches regex
    // 4. Use regex only for validation
    if (REGEX.test(target)) {
        res.redirect(target);
    } else {
        // 5. Redirect to root if validation fails
        res.redirect('/');
    }
});

module.exports = app;
