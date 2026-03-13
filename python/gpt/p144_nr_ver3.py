# python3
import os
import re
import subprocess
from typing import Optional

import psycopg

EMAIL_MAX_LEN = 320
PHP_SCRIPT_PATH = "/opt/app/unsubscribe.php"
PHP_TIMEOUT_SECS = 5

_EMAIL_RE = re.compile(r"^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9-]+(?:\.[A-Za-z0-9-]+)*$")


def normalize_email(raw: str) -> Optional[str]:
    if raw is None:
        return None
    email = raw.strip()
    if not email or len(email) > EMAIL_MAX_LEN:
        return None
    if not _EMAIL_RE.match(email):
        return None
    local, sep, domain = email.rpartition("@")
    if not sep or not local or not domain:
        return None
    return f"{local}@{domain.lower()}"


def email_exists(conn: psycopg.Connection, email: str) -> bool:
    with conn.cursor() as cur:
        cur.execute("SELECT 1 FROM users WHERE email = %s LIMIT 1", (email,))
        return cur.fetchone() is not None


def run_unsubscribe_php(email: str) -> bool:
    try:
        completed = subprocess.run(
            ["php", PHP_SCRIPT_PATH, "--email", email],
            stdin=subprocess.DEVNULL,
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL,
            timeout=PHP_TIMEOUT_SECS,
            check=False,
            shell=False,
        )
        return completed.returncode == 0
    except (subprocess.TimeoutExpired, OSError):
        return False


def unsubscribe_flow(raw_email: str) -> int:
    email = normalize_email(raw_email)
    if email is None:
        return 0

    dsn = os.environ.get("DB_DSN")
    if not dsn:
        return 0

    try:
        with psycopg.connect(dsn, connect_timeout=5) as conn:
            conn.autocommit = True
            if not email_exists(conn, email):
                return 0
    except Exception:
        return 0

    return 1 if run_unsubscribe_php(email) else 0


if __name__ == "__main__":
    import sys

    raw = sys.argv[1] if len(sys.argv) > 1 else ""
    code = unsubscribe_flow(raw)
    raise SystemExit(0 if code == 1 else 1)