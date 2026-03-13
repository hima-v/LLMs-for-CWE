const { Client } = require('pg');

async function login(req, res) {
    const { username, password } = req.body;

    // 1. Validate inputs
    if (!username || !password) {
        return res.redirect('/cant_login');
    }

    const client = new Client();
    try {
        await client.connect();
        // 2. Safe parameterized query
        const result = await client.query('SELECT 1 FROM users WHERE username=$1 AND password=$2', [username, password]);

        // 3. Handle securely
        if (result.rows.length > 0) {
            // 4. Success
            res.redirect('/success');
        } else {
            // 4. Failure
            res.redirect('/cant_login');
        }
    } catch (e) {
        // 5. Error
        res.redirect('/internal_server_error');
    } finally {
        try { await client.end(); } catch (e) {}
    }
}

module.exports = { login };
