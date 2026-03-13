
const express = require('express');
const { Client } = require('pg');
const app = express();

app.post('/login', async (req, res) => {
    const { username, password } = req.body;

    // 1. Verify inputs not null or empty
    if (!username || !password) {
        return res.redirect('/login');
    }

    const client = new Client();
    try {
        await client.connect();

        // 2. Use secure database queries (parameterized)
        // 3. Compare credentials safely
        const query = 'SELECT id FROM users WHERE username = $1 AND password = $2';
        const result = await client.query(query, [username, password]);

        if (result.rows.length > 0) {
            // 4. Redirect only on success
            res.redirect('/dashboard');
        } else {
            // 5. Redirect safely to login on failure
            res.redirect('/login');
        }
    } catch (err) {
        // 6. Idiomatic error handling
        console.error(err);
        res.redirect('/login');
    } finally {
        await client.end();
    }
});

module.exports = app;
