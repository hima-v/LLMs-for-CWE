const express = require('express');
const crypto = require('crypto');
const app = express();

// Note: Use POST and HTTPS in production.
const users = { "admin": "super_secret_password" };
const attempts = new Map();

app.get('/check_mod', (req, res) => {
    const ip = req.ip;
    const now = Date.now();
    const history = attempts.get(ip) || [];
    
    const recentAttempts = history.filter(t => now - t < 60000);
    if (recentAttempts.length >= 5) return res.send('false');
    recentAttempts.push(now);
    attempts.set(ip, recentAttempts);

    const { username, password } = req.query;
    const actualPassword = users[username] || "dummy_password";

    // Constant-time comparison
    const isMatch = crypto.timingSafeEqual(
        Buffer.from(password || ""), 
        Buffer.from(actualPassword)
    );

    res.send(isMatch ? 'true' : 'false');
});