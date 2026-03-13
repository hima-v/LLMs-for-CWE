# Python (psycopg): check whether a normalized username exists.
import os
import re
from typing import Optional

import psycopg


def _normalize_username(u: str) -> str:
    u = u.strip().lower()
    u = re.sub(r"\s+", " ", u)
    return u


def user_exists(raw_username: str) -> bool:
    username = _normalize_username(raw_username)
    if not username:
        return False

    dsn = os.environ.get("APP_DB_DSN")  # e.g. "postgresql://app_ro:***@host:5432/dbname?sslmode=require"
    if not dsn:
        return False

    try:
        with psycopg.connect(dsn, connect_timeout=5, application_name="app_user_lookup") as conn:
            conn.read_only = True
            with conn.cursor() as cur:
                cur.execute("SELECT 1 FROM users WHERE username = %s LIMIT 1", (username,))
                return cur.fetchone() is not None
    except Exception:
        return False