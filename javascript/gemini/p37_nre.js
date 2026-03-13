const express = require('express');
const { Client } = require('pg');
const app = express();

app.post('/login', async (req, res) => {
    const { username, password } = req.body;

    if (!username || !password) {
        return res.redirect('/cant_login');
    }

    const client = new Client();

    try {
        await client.connect();

        const query = 'SELECT id FROM users WHERE username = $1 AND password = $2';
        const result = await client.query(query, [username, password]);

        if (result.rows.length > 0) {
            res.redirect('/success');
        } else {
            res.redirect('/cant_login');
        }
    } catch (err) {
        console.error(err);
        res.redirect('/internal_server_error');
    } finally {
        // Ensure client is closed only if connected/created, but error logic handles redirect
        try { await client.end(); } catch(e) {}
    }
});

module.exports = app;
