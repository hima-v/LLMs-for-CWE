const { spawnSync } = require('child_process');

function manageUnsubscribe(email, db) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) return 0;

    const user = db.prepare('SELECT id FROM users WHERE email = ?').get(email);
    if (!user) return 0;

    // Securely spawn process
    const result = spawnSync('/usr/bin/php', ['/path/to/unsubscribe.php', email], { timeout: 5000 });
    return (result.stdout.toString().trim() === '0') ? 1 : 0;
}