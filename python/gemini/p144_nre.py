import sqlite3
import subprocess
import re

def manage_unsubscribe(email):
    if not re.match(r"[^@]+@[^@]+\.[^@]+", email):
        return 0
    try:
        conn = sqlite3.connect('users.db')
        cursor = conn.cursor()
        cursor.execute("SELECT 1 FROM subscribers WHERE email = ?", (email,))
        if not cursor.fetchone():
            return 0
        
        result = subprocess.run(
            ['/usr/bin/php', '/opt/app/unsub.php', email],
            capture_output=True,
            timeout=5
        )
        return 1 if result.stdout.decode().strip() == '0' else 0
    except Exception:
        return 0
    finally:
        conn.close()