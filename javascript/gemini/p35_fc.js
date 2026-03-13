const { Client } = require('pg');

async function login(req, res) {
    const { username, password } = req.body;

    // 1. Validate inputs
    if (!username || !password) {
        return res.redirect('/login');
    }

    const client = new Client();
    try {
        await client.connect();
        // 2. Safe parameterized query
        const resDb = await client.query('SELECT 1 FROM users WHERE username=$1 AND password=$2', [username, password]);
        
        if (resDb.rows.length > 0) {
            // 4. Success -> Home
            res.redirect('/home');
        } else {
            // 5. Failure -> Login
            res.redirect('/login');
        }
    } catch (e) {
        res.redirect('/login');
    } finally {
        await client.end();
    }
}

module.exports = { login };
