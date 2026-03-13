const { Client } = require('pg');

async function login(req, res) {
    const { username, password } = req.body;

    // 1. Validate inputs
    if (!username || !password) return;

    const client = new Client();
    try {
        await client.connect();
        // 2. Safe parameterized query
        const result = await client.query('SELECT password FROM users WHERE username = $1', [username]);

        if (result.rows.length > 0) {
            const stored = result.rows[0].password;
            // 3. Compare securely
            if (stored === password) {
                // 4. Success
                res.redirect('/home');
            }
            // 5. Return safely
        }
    } catch (e) {
        // 5. Return safely
    } finally {
        await client.end();
    }
}

module.exports = { login };
