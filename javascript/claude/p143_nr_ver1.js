// JavaScript (Node.js/Express)
const express = require('express');
const sqlite3 = require('sqlite3').verbose();
const crypto = require('crypto');
const { validate: validateEmail } = require('email-validator');

const app = express();
const SECRET_KEY = 'your-secret-key-change-this';
const DB_PATH = 'users.db';

function generateUnsubscribeToken(email) {
    const timestamp = Math.floor(Date.now() / 1000).toString();
    const message = `${email}:${timestamp}`;
    const signature = crypto
        .createHmac('sha256', SECRET_KEY)
        .update(message)
        .digest('hex');
    return `${timestamp}:${signature}`;
}

function verifyUnsubscribeToken(email, token) {
    try {
        const [timestamp, signature] = token.split(':');
        const tokenTime = parseInt(timestamp);
        const currentTime = Math.floor(Date.now() / 1000);
        
        if (currentTime - tokenTime > 3600) return false;
        
        const message = `${email}:${timestamp}`;
        const expectedSignature = crypto
            .createHmac('sha256', SECRET_KEY)
            .update(message)
            .digest('hex');
        
        return crypto.timingSafeEqual(
            Buffer.from(signature),
            Buffer.from(expectedSignature)
        );
    } catch {
        return false;
    }
}

function validateEmailFormat(email) {
    if (!email || email.length > 254) return null;
    const normalized = email.toLowerCase().trim();
    return validateEmail(normalized) ? normalized : null;
}

app.get('/unsubscribe', (req, res) => {
    const email = (req.query.email || '').trim();
    const token = (req.query.token || '').trim();
    
    if (!email || !token || email.length > 254) {
        return res.status(200).json({ message: "We've processed your request." });
    }
    
    const normalizedEmail = validateEmailFormat(email);
    if (!normalizedEmail) {
        return res.status(200).json({ message: "We've processed your request." });
    }
    
    if (!verifyUnsubscribeToken(normalizedEmail, token)) {
        return res.status(200).json({ message: "We've processed your request." });
    }
    
    const db = new sqlite3.Database(DB_PATH, (err) => {
        if (err) {
            return res.status(200).json({ message: "We've processed your request." });
        }
        
        db.run('DELETE FROM users WHERE email = ?', [normalizedEmail], (err) => {
            db.close();
            res.status(200).json({ message: "We've processed your request." });
        });
    });
});

app.listen(3000, () => console.log('Server running on port 3000'));