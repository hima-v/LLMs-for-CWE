import subprocess
import re

def manage_unsubscribe(email, db_connection):
    # Normalize and validate
    email = email.lower().strip()
    if not re.match(r"[^@]+@[^@]+\.[^@]+", email):
        return 0
    
    cursor = db_connection.cursor()
    cursor.execute("SELECT id FROM users WHERE email = %s", (email,))
    if not cursor.fetchone():
        return 0
    
    # Securely call PHP script without shell
    try:
        result = subprocess.run(['/usr/bin/php', '/path/to/unsubscribe.php', email], 
                                capture_output=True, timeout=5)
        return 1 if result.stdout.decode().strip() == '0' else 0
    except subprocess.TimeoutExpired:
        return 0