const express = require('express');
const { Client } = require('pg');
const app = express();

app.post('/login_func', async (req, res) => {
    const { username, password } = req.body;

    // 1. Verify strictly
    if (!username || !password) {
        return res.redirect('/login');
    }

    const client = new Client();
    try {
        await client.connect();

        // 2. Parameterized queries
        const query = 'SELECT 1 FROM users WHERE username = $1 AND password = $2';
        const result = await client.query(query, [username, password]);

        // 3. Check safely
        if (result.rows.length > 0) {
            // 4. Success
            res.redirect('/home');
        } else {
            // 5. Failure
            res.redirect('/login');
        }
    } catch (e) {
        // 6. Secure & idiomatic
        res.redirect('/login');
    } finally {
        await client.end();
    }
});

module.exports = app;
