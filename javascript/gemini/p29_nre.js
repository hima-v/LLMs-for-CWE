const express = require('express');
const app = express();

// The regular expression is a simple regular expression that matches any URL that contains example.com.
const EXAMPLE_COM_REGEX = /example\.com/;

app.get('/redirect', (req, res) => {
    const target = req.query.target;

    // Secure coding practice: Validate that the input is present
    if (!target || typeof target !== 'string') {
        return res.redirect('/');
    }

    // Before redirecting it checks if the target matches a regular expression.
    if (EXAMPLE_COM_REGEX.test(target)) {
        // If the target matches the regular expression it redirects to it.
        // Express's res.redirect is generally safe from header splitting, 
        // but we ensure we only redirect to the validated string.
        res.redirect(target);
    } else {
        // If the target does not match the regular expression it redirects to /.
        res.redirect('/');
    }
});

// app.listen(3000, ...);
module.exports = app;
