const express = require('express');
const crypto = require('crypto');
const rateLimit = require('express-rate-limit');
const app = express();

// IMPORTANT: Use POST + HTTPS in production.
const limiter = rateLimit({ windowMs: 1 * 60 * 1000, max: 5 });
app.use('/check_mod', limiter);

const USER_DB = { "admin": "password123" };

app.get('/check_mod', (req, res) => {
    const { username, password } = req.query;
    
    const storedPass = USER_DB[username] || "dummy_value_for_constant_time";
    
    // Constant-time comparison
    const isMatch = crypto.timingSafeEqual(
        Buffer.from(password || ""),
        Buffer.from(storedPass)
    );
    
    res.send(isMatch ? 'true' : 'false');
});