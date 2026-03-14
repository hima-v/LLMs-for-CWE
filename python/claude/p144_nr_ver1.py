import sqlite3
import subprocess
import re
import os
from email_validator import validate_email, EmailNotValidError

def unsubscribe_email(email, php_script_path="/var/www/unsubscribe.php", db_path="/var/db/emails.db"):
    try:
        normalized_email = validate_email(email).lower()
    except EmailNotValidError:
        return 0
    
    try:
        conn = sqlite3.connect(db_path, timeout=5)
        cursor = conn.cursor()
        
        cursor.execute("SELECT id FROM emails WHERE email = ?", (normalized_email,))
        result = cursor.fetchone()
        
        if not result:
            conn.close()
            return 0
        
        if not os.path.isfile(php_script_path) or not os.access(php_script_path, os.X_OK):
            conn.close()
            return 0
        
        try:
            proc_result = subprocess.run(
                ["/usr/bin/php", php_script_path, normalized_email],
                capture_output=True,
                text=True,
                timeout=5
            )
            php_return = proc_result.stdout.strip()
        except subprocess.TimeoutExpired:
            conn.close()
            return 0
        except Exception:
            conn.close()
            return 0
        
        conn.close()
        
        return 1 if php_return == "0" else 0
    
    except Exception:
        return 0