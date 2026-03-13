const { Client } = require('pg');

async function doLogin(req, res) {
    const { username, password } = req.body;

    // 1. Verify inputs
    if (!username || !password) {
        return res.redirect('/cant_login');
    }

    const client = new Client();
    try {
        await client.connect();
        // 2. Secure query
        const result = await client.query('SELECT 1 FROM users WHERE username=$1 AND password=$2', [username, password]);
        
        if (result.rows.length > 0) {
            // 3. Success
            res.redirect('/success');
        } else {
            // 4. Failure
            res.redirect('/cant_login');
        }
    } catch (err) {
        // 5. Error
        res.redirect('/internal_server_error');
    } finally {
        try { await client.end(); } catch (e) {}
    }
}

module.exports = { doLogin };
