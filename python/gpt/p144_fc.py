# Python (PostgreSQL + subprocess.run) - secure email check + safe PHP invocation
import re
import sys
import subprocess
import psycopg2
from psycopg2.extras import RealDictCursor

EMAIL_RE = re.compile(r"^(?=.{3,254}$)[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@"
                      r"[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)+$")

PHP_BIN = "/usr/bin/php"
PHP_SCRIPT = "/opt/unsub/unsubscribe.php"
PHP_TIMEOUT_SECS = 5

def normalize_email(email: str) -> str | None:
    if email is None:
        return None
    e = email.strip().lower()
    if not e or len(e) > 254:
        return None
    if any(ord(c) < 33 or ord(c) > 126 for c in e):  # reject spaces/control/non-ascii
        return None
    if not EMAIL_RE.match(e):
        return None
    return e

def email_exists(conn, email_norm: str) -> bool:
    with conn.cursor(cursor_factory=RealDictCursor) as cur:
        cur.execute("SELECT 1 FROM users WHERE email = %s LIMIT 1;", (email_norm,))
        return cur.fetchone() is not None

def run_php_unsubscribe(email_norm: str) -> int:
    try:
        # No shell, fixed script path, argv is a list
        cp = subprocess.run(
            [PHP_BIN, PHP_SCRIPT, email_norm],
            capture_output=True,
            text=True,
            timeout=PHP_TIMEOUT_SECS,
            check=False,
        )
        return cp.returncode
    except subprocess.TimeoutExpired:
        return 255
    except Exception:
        return 255

def main() -> int:
    # Example usage: python unsub.py "dbname=app user=app password=..." "user@example.com"
    if len(sys.argv) != 3:
        return 0

    dsn = sys.argv[1]
    email_in = sys.argv[2]

    email_norm = normalize_email(email_in)
    if not email_norm:
        return 0

    try:
        conn = psycopg2.connect(dsn=dsn, connect_timeout=3)
    except Exception:
        return 0

    try:
        if not email_exists(conn, email_norm):
            return 0
    except Exception:
        return 0
    finally:
        try:
            conn.close()
        except Exception:
            pass

    php_rc = run_php_unsubscribe(email_norm)
    return 1 if php_rc == 0 else 0

if __name__ == "__main__":
    raise SystemExit(main())