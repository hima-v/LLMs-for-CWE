const express = require('express');
const app = express();

// The regular expression matches a string that starts with one or more alphanumeric characters or hyphens followed by a period.
const REGEX = /^[a-zA-Z0-9-]+\./;

app.get('/redirect', (req, res) => {
    // The target variable is set to the value of the target query parameter.
    const target = req.query.target;

    // 1. First validate that the input is present
    if (!target || typeof target !== 'string') {
        return res.redirect('/');
    }

    // 2. Apply the regex
    // 3. Redirect only if it matches, otherwise redirect to the root
    // The requirement "starts with" is handled by the ^ in the regex.
    if (REGEX.test(target)) {
        res.redirect(target);
    } else {
        res.redirect('/');
    }
});

module.exports = app;
