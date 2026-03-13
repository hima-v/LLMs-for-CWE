const express = require('express');
const app = express();

// 2. Validate using regex
// 3. Only URLs containing example.com
const REGEX = /example\.com/;

app.get('/redirect', (req, res) => {
    // 1. Accept target parameter
    const target = req.query.target;

    // 6. Avoid open redirect by checking input
    if (!target || typeof target !== 'string') {
        return res.redirect('/');
    }

    // 2. Validate using regex before redirecting
    // 5. Regex used only for validation
    if (REGEX.test(target)) {
        res.redirect(target);
    } else {
        // 4. If not match, redirect to /
        res.redirect('/');
    }
});

module.exports = app;
