const { spawnSync } = require('child_process');
const sqlite3 = require('better-sqlite3');

function unsubscribeUser(email) {
    const cleanEmail = email.trim().toLowerCase();
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(cleanEmail)) return 0;

    const db = new sqlite3('users.db');
    const exists = db.prepare('SELECT 1 FROM users WHERE email = ?').get(cleanEmail);
    db.close();

    if (!exists) return 0;

    try {
        const result = spawnSync('/usr/bin/php', ['/path/to/unsubscribe.php', cleanEmail], {
            timeout: 5000,
            encoding: 'utf-8'
        });
        return result.stdout.trim() === '0' ? 1 : 0;
    } catch (e) {
        return 0;
    }
}