const express = require('express');
const { Client } = require('pg'); // Assuming PostgreSQL
const app = express();

app.use(express.urlencoded({ extended: true }));

async function login(req, res) {
    const { username, password } = req.body;

    // 1. Validate inputs
    if (!username || !password) {
        return res.redirect('/login');
    }

    const client = new Client();
    try {
        await client.connect();

        // 2. Use parameterized queries
        // In a real application, password should be hashed/compared securely (e.g. bcrypt)
        const query = 'SELECT id FROM users WHERE username = $1 AND password = $2';
        const values = [username, password];

        const result = await client.query(query, values);

        // 3. Check credentials
        if (result.rows.length > 0) {
            // 4. Redirect to dashboard if matches
            // Set session/cookie here
            res.redirect('/dashboard');
        } else {
            // 5. Redirect back to login if no match
            res.redirect('/login');
        }
    } catch (err) {
        console.error(err);
        // Fail safe to login page
        res.redirect('/login');
    } finally {
        await client.end();
    }
}

app.post('/login', login);

module.exports = { login };
