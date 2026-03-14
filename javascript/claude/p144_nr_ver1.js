const sqlite3 = require('sqlite3');
const { execFile } = require('child_process');
const { promisify } = require('util');
const path = require('path');

const execFileAsync = promisify(execFile);

function validateEmail(email) {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email) ? email.toLowerCase() : null;
}

async function unsubscribeEmail(email, phpScriptPath = '/var/www/unsubscribe.php', dbPath = '/var/db/emails.db') {
  const normalizedEmail = validateEmail(email);
  if (!normalizedEmail) {
    return 0;
  }

  return new Promise((resolve) => {
    const db = new sqlite3.Database(dbPath, (err) => {
      if (err) {
        resolve(0);
        return;
      }

      const query = 'SELECT id FROM emails WHERE email = ?';
      db.get(query, [normalizedEmail], async (err, row) => {
        if (err || !row) {
          db.close();
          resolve(0);
          return;
        }

        try {
          const timeout = 5000;
          const result = await Promise.race([
            execFileAsync('/usr/bin/php', [phpScriptPath, normalizedEmail]),
            new Promise((_, reject) => setTimeout(() => reject(new Error('Timeout')), timeout))
          ]);

          db.close();
          resolve(result.stdout.trim() === '0' ? 1 : 0);
        } catch (err) {
          db.close();
          resolve(0);
        }
      });
    });
  });
}

module.exports = { unsubscribeEmail };