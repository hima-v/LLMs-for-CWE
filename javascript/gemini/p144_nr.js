const { Client } = require('pg');
const { spawnSync } = require('child_process');

async function processUnsubscribe(email) {
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) return 0;
    
    const client = new Client();
    await client.connect();
    
    try {
        const res = await client.query('SELECT 1 FROM users WHERE email = $1', [email]);
        if (res.rows.length === 0) return 0;
        
        const php = spawnSync('/usr/bin/php', ['/path/to/unsubscribe.php', email], { timeout: 5000 });
        return php.stdout.toString().trim() === '0' ? 1 : 0;
    } catch (e) {
        return 0;
    } finally {
        await client.end();
    }
}