# python3
import re
import sqlite3
import subprocess
from pathlib import Path

DB_PATH = Path("/var/app/data/app.db")  # fixed path
PHP_BIN = Path("/usr/bin/php")         # fixed path
PHP_SCRIPT = Path("/var/app/scripts/unsubscribe.php")  # fixed path
PHP_TIMEOUT_SECS = 3

EMAIL_RE = re.compile(r"^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9-]+(?:\.[A-Za-z0-9-]+)+$")

def normalize_email(email: str) -> str:
    if email is None:
        return ""
    email = email.strip().lower()
    return email

def is_valid_email(email: str) -> bool:
    if not email or len(email) > 254:
        return False
    return EMAIL_RE.match(email) is not None

def email_exists(conn: sqlite3.Connection, email: str) -> bool:
    cur = conn.cursor()
    cur.execute("SELECT 1 FROM subscribers WHERE email = ? LIMIT 1", (email,))
    return cur.fetchone() is not None

def call_php_unsubscribe(email: str) -> bool:
    if not PHP_BIN.is_file() or not PHP_SCRIPT.is_file():
        return False

    try:
        p = subprocess.run(
            [str(PHP_BIN), str(PHP_SCRIPT), email],
            capture_output=True,
            text=True,
            timeout=PHP_TIMEOUT_SECS,
            check=False,
        )
    except subprocess.TimeoutExpired:
        return False

    if p.returncode != 0:
        return False

    out = (p.stdout or "").strip()
    return out == "0"

def unsubscribe_flow(email_input: str) -> int:
    email = normalize_email(email_input)
    if not is_valid_email(email):
        return 0

    try:
        conn = sqlite3.connect(str(DB_PATH))
        conn.execute("PRAGMA foreign_keys = ON")
    except Exception:
        return 0

    try:
        if not email_exists(conn, email):
            return 0
        ok = call_php_unsubscribe(email)
        return 1 if ok else 0
    finally:
        try:
            conn.close()
        except Exception:
            pass

if __name__ == "__main__":
    import sys
    email_arg = sys.argv[1] if len(sys.argv) > 1 else ""
    print(unsubscribe_flow(email_arg))