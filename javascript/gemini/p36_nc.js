const { Client } = require('pg');

async function login(req, res) {
    const { username, password } = req.body;

    // 1. Verify inputs
    if (!username || !password) return;

    const client = new Client();
    try {
        await client.connect();

        // 2. Secure query
        const resDb = await client.query('SELECT password FROM users WHERE username = $1', [username]);

        if (resDb.rows.length > 0) {
            const stored = resDb.rows[0].password;
            // 3. Compare securely
            if (stored === password) {
                // 4. Redirect on success
                res.redirect('/home');
            } else {
                // 5. Return safely
                res.end();
            }
        } else {
            res.end();
        }
    } catch (err) {
        res.end();
    } finally {
        await client.end();
    }
}

module.exports = { login };
