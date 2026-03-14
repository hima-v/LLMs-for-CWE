# Python
import re
import subprocess
import sqlite3
import signal
from pathlib import Path

def validate_email(email: str) -> str:
    email = email.strip().lower()
    if not re.match(r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$', email):
        raise ValueError("Invalid email")
    return email

def check_and_unsubscribe(email: str, db_path: str, php_script: str) -> int:
    try:
        email = validate_email(email)
        conn = sqlite3.connect(db_path)
        cursor = conn.cursor()
        cursor.execute("SELECT 1 FROM users WHERE email = ?", (email,))
        exists = cursor.fetchone() is not None
        conn.close()
        
        if not exists:
            return 0
        
        try:
            result = subprocess.run(
                ['/usr/bin/php', php_script, email],
                capture_output=True,
                timeout=5,
                check=False
            )
            return 1 if result.returncode == 0 else 0
        except subprocess.TimeoutExpired:
            return 0
    except Exception:
        return 0