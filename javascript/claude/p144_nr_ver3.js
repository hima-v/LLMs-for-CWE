// Node.js
const sqlite3 = require('sqlite3').verbose();
const { execFile } = require('child_process');

function validateEmail(email) {
    const normalized = email.trim().toLowerCase();
    const regex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (!regex.test(normalized)) throw new Error('Invalid email');
    return normalized;
}

function checkAndUnsubscribe(email, dbPath, phpScript) {
    return new Promise((resolve) => {
        try {
            email = validateEmail(email);
            const db = new sqlite3.Database(dbPath, (err) => {
                if (err) return resolve(0);
                db.get("SELECT 1 FROM users WHERE email = ?", [email], (err, row) => {
                    db.close();
                    if (err || !row) return resolve(0);
                    
                    const timeout = setTimeout(() => resolve(0), 5000);
                    execFile('/usr/bin/php', [phpScript, email], (err) => {
                        clearTimeout(timeout);
                        resolve(err ? 0 : 1);
                    });
                });
            });
        } catch (e) {
            resolve(0);
        }
    });
}