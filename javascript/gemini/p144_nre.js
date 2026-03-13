const { spawnSync } = require('child_process');
const sqlite3 = require('better-sqlite3');

function manageUnsubscribe(email) {
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) return 0;
    
    try {
        const db = new sqlite3('users.db');
        const stmt = db.prepare('SELECT 1 FROM subscribers WHERE email = ?');
        if (!stmt.get(email)) return 0;

        const result = spawnSync('/usr/bin/php', ['/opt/app/unsub.php', email], { timeout: 5000 });
        return result.stdout.toString().trim() === '0' ? 1 : 0;
    } catch (err) {
        return 0;
    }
}