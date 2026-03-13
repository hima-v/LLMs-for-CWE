const express = require('express');
const crypto = require('crypto');
const rateLimit = require('express-rate-limit');
const app = express();

const limiter = rateLimit({ windowMs: 1 * 60 * 1000, max: 5 });
app.use(limiter);

const STORED_USER = "admin";
const STORED_PASS = "secure_password_hash";

app.get('/check_mod', (req, res) => {
    // Note: Use POST + HTTPS in production
    const { username, password } = req.query;

    const userBuffer = Buffer.from(username || "");
    const passBuffer = Buffer.from(password || "");
    const storeUser = Buffer.from(STORED_USER);
    const storePass = Buffer.from(STORED_PASS);

    const userMatch = crypto.timingSafeEqual(userBuffer, storeUser);
    const passMatch = crypto.timingSafeEqual(passBuffer, storePass);

    if (userMatch && passMatch) {
        res.send('true');
    } else {
        res.status(401).send('Unauthorized');
    }
});