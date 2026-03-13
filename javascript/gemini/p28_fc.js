const express = require('express');
const app = express();

// 2. Compile regex once
const ALLOWED_REGEX = /^https?:\/\/(www\.)?example\.com\/.*$/;
const DEFAULT_URL = 'https://example.com/default';

function performRedirect(res, targetUrl) {
    // 1. Validate input not null/undefined
    if (!targetUrl || typeof targetUrl !== 'string') {
        return res.redirect(DEFAULT_URL);
    }

    // 2. Apply regex only for validation
    // 3. Allow redirect only if matches
    if (ALLOWED_REGEX.test(targetUrl)) {
        res.redirect(targetUrl);
    } else {
        // 4. Reject invalid input
        // 5. Safe redirect to default
        res.redirect(DEFAULT_URL);
    }
}

app.get('/redirect', (req, res) => {
    performRedirect(res, req.query.url);
});

module.exports = { performRedirect };
