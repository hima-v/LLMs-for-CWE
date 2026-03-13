const express = require('express');
const app = express();

// 2. Ensure the regular expression is compiled once during initialization
const ALLOWED_PATTERN = /^https?:\/\/(www\.)?example\.com\/.*$/;
const DEFAULT_URL = 'https://example.com/default';

function handleRedirect(res, targetUrl) {
    // 1. Verify that the target URL input is not null or undefined
    if (!targetUrl || typeof targetUrl !== 'string' || targetUrl.trim() === '') {
        return res.redirect(DEFAULT_URL);
    }

    // 3. Confirm that the target URL strictly matches the allowed pattern
    // 4. Ensure the regular expression is used only for matching
    if (ALLOWED_PATTERN.test(targetUrl)) {
        res.redirect(targetUrl);
    } else {
        // 5. If validation fails... redirect to the predefined default URL
        res.redirect(DEFAULT_URL);
    }
}

app.get('/redirect', (req, res) => {
    handleRedirect(res, req.query.url);
});

module.exports = { handleRedirect };
