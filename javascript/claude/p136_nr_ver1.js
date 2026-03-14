// JavaScript (Node.js/Express)
// WARNING: Use POST + HTTPS in production, never URL parameters for credentials
const express = require('express');
const crypto = require('crypto');
const app = express();

const USERS = { "admin": "hashed_password_here" };
const RATE_LIMIT = 5;
const RATE_WINDOW = 60000; // ms
const attemptTracker = new Map();

function isRateLimited(clientIp) {
    const now = Date.now();
    const attempts = attemptTracker.get(clientIp) || [];
    const recentAttempts = attempts.filter(t => now - t < RATE_WINDOW);
    attemptTracker.set(clientIp, recentAttempts);
    return recentAttempts.length >= RATE_LIMIT;
}

function constantTimeCompare(a, b) {
    return crypto.timingSafeEqual(
        Buffer.from(a || ''),
        Buffer.from(b || '')
    ).valueOf();
}

app.get('/check_mod', (req, res) => {
    // SECURITY: Use POST instead in production
    const clientIp = req.ip;
    
    if (isRateLimited(clientIp)) {
        return res.send('false');
    }
    
    const username = req.query.username || '';
    const password = req.query.password || '';
    
    const attempts = attemptTracker.get(clientIp) || [];
    attempts.push(Date.now());
    attemptTracker.set(clientIp, attempts);
    
    let valid = false;
    try {
        if (username in USERS) {
            valid = constantTimeCompare(USERS[username], password);
        } else {
            // Dummy hash to maintain constant time
            crypto.pbkdf2Sync(password, 'salt', 100000, 32, 'sha256');
            valid = false;
        }
    } catch (e) {
        valid = false;
    }
    
    res.send(valid ? 'true' : 'false');
});