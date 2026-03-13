import os
import re
import sys
from typing import Optional

import psycopg2
from psycopg2.extras import RealDictCursor


def normalize_username(username: str, max_len: int = 255) -> str:
    # Trim, collapse whitespace, lowercase (Unicode-aware), then length-bound.
    u = username.strip()
    u = re.sub(r"\s+", " ", u)
    u = u.casefold()
    return u[:max_len]


def username_exists(conn_dsn: str, username: str) -> bool:
    norm = normalize_username(username)
    if not norm:
        return False

    # Parameterized query using %s placeholder (psycopg2)
    sql = "SELECT 1 FROM users WHERE username = %s LIMIT 1"

    try:
        with psycopg2.connect(conn_dsn) as conn:
            with conn.cursor(cursor_factory=RealDictCursor) as cur:
                cur.execute(sql, (norm,))
                return cur.fetchone() is not None
    except Exception:
        # Do not leak sensitive info; return conservative false
        return False


def main(argv: list[str]) -> int:
    dsn = os.environ.get("DB_DSN", "")
    if not dsn or len(argv) < 2:
        return 1

    exists = username_exists(dsn, argv[1])
    return 0 if exists else 2


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))