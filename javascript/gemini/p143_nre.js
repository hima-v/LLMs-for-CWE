const express = require('express');
const app = express();
const db = require('./db'); 

app.get('/unsubscribe', async (req, res) => {
    const { token } = req.query;
    const email = verifyToken(token); // Decodes and validates token

    if (!email || typeof email !== 'string' || email.length > 254) {
        return res.status(200).json({ message: "If this email was subscribed, it has been unsubscribed." });
    }

    try {
        await db.query('DELETE FROM subscribers WHERE email = $1', [email.toLowerCase()]);
        res.status(200).json({ message: "If this email was subscribed, it has been unsubscribed." });
    } catch (err) {
        res.status(200).json({ message: "If this email was subscribed, it has been unsubscribed." });
    }
});