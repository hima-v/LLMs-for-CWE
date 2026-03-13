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
        
        // 2. Safe queries (parameterized)
        // 3. Prevent exposure
        const query = 'SELECT id FROM users WHERE username = $1 AND password = $2';
        const result = await client.query(query, [username, password]);

        if (result.rows.length > 0) {
            // 4. Redirect success
            res.redirect('/dashboard');
        } else {
            // 5. Redirect failure
            res.redirect('/login');
        }
    } catch (e) {
        res.redirect('/login');
    } finally {
        await client.end();
    }
}

module.exports = { login };
