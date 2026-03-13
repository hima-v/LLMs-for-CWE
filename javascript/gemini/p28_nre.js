const express = require('express');
const app = express();

// The regular expression is compiled once when the module is loaded
const ALLOWED_URL_REGEX = /^https?:\/\/(www\.)?example\.com\/.*$/;
const DEFAULT_URL = 'https://example.com/default';

function performRedirect(res, targetUrl) {
    // 1. Validate that the target URL input is present
    if (!targetUrl || typeof targetUrl !== 'string') {
        return res.redirect(DEFAULT_URL);
    }

    // 2. Apply the compiled regular expression to check whether the URL is allowed
    // 3. If the URL matches the pattern, use it as the redirect target
    if (ALLOWED_URL_REGEX.test(targetUrl)) {
        return res.redirect(targetUrl);
    }

    // 4. If it does not match or validation fails, redirect to the default URL instead
    return res.redirect(DEFAULT_URL);
}

// Example route using the logic
app.get('/redirect', (req, res) => {
    const target = req.query.url;
    performRedirect(res, target);
});

// For demonstration purposes (not running a server)
// console.log("Module loaded with compiled regex.");
module.exports = { performRedirect };
