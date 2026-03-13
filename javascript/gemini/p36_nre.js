const express = require('express');
const { Client } = require('pg');
const app = express();

app.post('/login', async (req, res) => {
    const { username, password } = req.body;

    // 1. Ensure inputs are present
    if (!username || !password) {
        return; // Return safely (send default response or end)
    }

    const client = new Client();
    try {
        await client.connect();

        // 2. Secure query to check username
        const query = 'SELECT password FROM users WHERE username = $1';
        const result = await client.query(query, [username]);

        if (result.rows.length > 0) {
            // Username exists
            const storedPassword = result.rows[0].password;

            // Check if password matches
            if (storedPassword === password) {
                // Redirect user to home page
                res.redirect('/home');
            } else {
                // Incorrect, return safely
                res.end();
            }
        } else {
            // Does not exist, return safely
            res.end();
        }

    } catch (err) {
        // Return safely
        console.error(err);
        res.end();
    } finally {
        await client.end();
    }
});

module.exports = app;
