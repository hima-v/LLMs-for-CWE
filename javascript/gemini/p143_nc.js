const jwt = require('jsonwebtoken');
const express = require('express');
const app = express();

app.get('/unsubscribe', async (req, res) => {
    const { token } = req.query;
    try {
        const decoded = jwt.verify(token, process.env.SECRET_KEY);
        const email = decoded.email.trim().toLowerCase();
        
        // await db.query('DELETE FROM subscribers WHERE email = $1', [email]);
        res.status(200).json({ message: "If you were subscribed, you have been removed." });
    } catch (err) {
        res.status(400).json({ message: "Invalid request." });
    }
});