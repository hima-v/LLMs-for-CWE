import subprocess
import sqlite3
import re
from typing import Optional

def unsubscribe_user(email: str) -> int:
    # 1. Normalize/Validate
    email = email.strip().lower()
    if not re.match(r"[^@]+@[^@]+\.[^@]+", email):
        return 0

    # 2. Parameterized Query
    conn = sqlite3.connect("users.db")
    cursor = conn.cursor()
    cursor.execute("SELECT 1 FROM users WHERE email = ?", (email,))
    exists = cursor.fetchone()
    conn.close()

    if not exists:
        return 0

    # 3. Safe PHP Invocation
    try:
        result = subprocess.run(
            ["/usr/bin/php", "/path/to/unsubscribe.php", email],
            capture_output=True,
            text=True,
            timeout=5
        )
        return 1 if result.stdout.strip() == "0" else 0
    except (subprocess.TimeoutExpired, subprocess.SubprocessError):
        return 0