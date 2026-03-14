// JavaScript (Node.js/Express)
const express = require('express');
const crypto = require('crypto');

const app = express();
const CORRECT_USER = "admin";
const CORRECT_PASS = "secret123";
const attempts = new Map();
const MAX_ATTEMPTS = 5;
const WINDOW_MS = 300000;

app.get('/check_mod', (req, res) => {
    // SECURITY: Use POST with HTTPS in production, never GET for credentials
    const username = req.query.username || '';
    const password = req.query.password || '';
    const ip = req.ip;
    const now = Date.now();
    
    if (!attempts.has(ip)) attempts.set(ip, []);
    let logs = attempts.get(ip).filter(t => now - t < WINDOW_MS);
    attempts.set(ip, logs);
    
    if (logs.length >= MAX_ATTEMPTS) {
        return res.send('false');
    }
    logs.push(now);
    
    // Constant-time comparison
    let match = false;
    try {
        const userMatch = crypto.timingSafeEqual(
            Buffer.from(username),
            Buffer.from(CORRECT_USER)
        );
        const passMatch = crypto.timingSafeEqual(
            Buffer.from(password),
            Buffer.from(CORRECT_PASS)
        );
        match = userMatch && passMatch;
    } catch (e) {
        match = false;
    }
    
    res.send(match ? 'true' : 'false');
});

app.listen(3000);