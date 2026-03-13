const express = require('express');
const crypto = require('crypto');
const app = express();

// SECURITY WARNING: Sending credentials in query parameters is insecure.
const STORED_USER = "admin";
const STORED_PASS = "secret_hash";

const rateLimit = new Map();

app.get('/check_mod', (req, res) => {
    const { username, password } = req.query;
    const ip = req.ip;

    // Basic throttling
    const now = Date.now();
    const attempts = rateLimit.get(ip) || 0;
    if (attempts > 5) return res.status(429).send('false');
    rateLimit.set(ip, attempts + 1);

    // Constant-time comparison
    const userMatch = crypto.timingSafeEqual(Buffer.from(username), Buffer.from(STORED_USER));
    const passMatch = crypto.timingSafeEqual(Buffer.from(password), Buffer.from(STORED_PASS));

    if (userMatch && passMatch) {
        res.send('true');
    } else {
        res.send('false');
    }
});