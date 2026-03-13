# Python 3 (sqlite3 + safe PHP exec)
import re
import sqlite3
import subprocess

DB_PATH = "/opt/app/app.db"
PHP_BIN = "/usr/bin/php"
PHP_SCRIPT = "/opt/app/unsub.php"
PHP_TIMEOUT_SEC = 5

_EMAIL_RE = re.compile(r"^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)+$")

def normalize_email(email: str) -> str | None:
    if email is None:
        return None
    e = email.strip().lower()
    if not e or len(e) > 254:
        return None
    if "\x00" in e:
        return None
    if not _EMAIL_RE.match(e):
        return None
    local, domain = e.split("@", 1)
    if len(local) > 64 or len(domain) > 255:
        return None
    return e

def email_exists(conn: sqlite3.Connection, email: str) -> bool:
    cur = conn.cursor()
    cur.execute("SELECT 1 FROM subscribers WHERE email = ? LIMIT 1", (email,))
    return cur.fetchone() is not None

def run_php_unsub(email: str) -> int:
    try:
        cp = subprocess.run(
            [PHP_BIN, PHP_SCRIPT, email],
            stdin=subprocess.DEVNULL,
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL,
            timeout=PHP_TIMEOUT_SEC,
            check=False,
        )
        return cp.returncode
    except Exception:
        return 1

def process_unsubscribe(raw_email: str) -> int:
    email = normalize_email(raw_email)
    if not email:
        return 0

    try:
        conn = sqlite3.connect(DB_PATH, timeout=2, isolation_level=None)
        try:
            if not email_exists(conn, email):
                return 0
        finally:
            conn.close()
    except Exception:
        return 0

    rc = run_php_unsub(email)
    return 1 if rc == 0 else 0

if __name__ == "__main__":
    import sys
    arg = sys.argv[1] if len(sys.argv) > 1 else ""
    sys.exit(0 if process_unsubscribe(arg) == 1 else 1)