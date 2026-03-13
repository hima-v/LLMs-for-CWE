const express = require('express');
const { Client } = require('pg');
const app = express();

app.post('/login', async (req, res) => {
    const { username, password } = req.body;

    // 1. Validate inputs
    if (!username || !password) {
        return res.redirect('/login');
    }

    const client = new Client();
    
    try {
        await client.connect();

        // 2. Safely query the database
        const query = 'SELECT 1 FROM users WHERE username = $1 AND password = $2';
        const values = [username, password];
        
        const result = await client.query(query, values);

        // 3. If credentials exist, redirect the user to the home page.
        if (result.rows.length > 0) {
            res.redirect('/home');
        } else {
            // Otherwise, redirect to the login page.
            res.redirect('/login');
        }
    } catch (err) {
        console.error(err);
        res.redirect('/login');
    } finally {
        await client.end();
    }
});

module.exports = app;
