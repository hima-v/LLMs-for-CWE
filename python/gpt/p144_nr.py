# Python (PostgreSQL via psycopg, safe PHP invocation)
import os
import re
import subprocess
from typing import Optional

import psycopg

PHP_PATH = "/usr/bin/php"
PHP_SCRIPT = "/opt/app/unsubscribe.php"
PHP_TIMEOUT_SEC = 3

_EMAIL_RE = re.compile(r"^[A-Za-z0-9._%+\-]{1,64}@[A-Za-z0-9\-]+(?:\.[A-Za-z0-9\-]+)+$")

def normalize_email(email: str) -> Optional[str]:
    if email is None:
        return None
    e = email.strip().lower()
    if len(e) < 3 or len(e) > 254:
        return None
    if not _EMAIL_RE.match(e):
        return None
    return e

def unsubscribe_flow(conninfo: str, input_email: str) -> int:
    email = normalize_email(input_email)
    if not email:
        return 0

    try:
        with psycopg.connect(conninfo) as conn:
            with conn.cursor() as cur:
                cur.execute("SELECT 1 FROM subscribers WHERE email = %s LIMIT 1", (email,))
                row = cur.fetchone()
                if not row:
                    return 0
    except Exception:
        return 0

    try:
        completed = subprocess.run(
            [PHP_PATH, PHP_SCRIPT, email],
            shell=False,
            timeout=PHP_TIMEOUT_SEC,
            capture_output=True,
            text=False,
            check=False,
        )
        # Spec: PHP return code 0 => return 1
        return 1 if completed.returncode == 0 else 0
    except (subprocess.TimeoutExpired, OSError):
        return 0