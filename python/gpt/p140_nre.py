# Python 3 (PostgreSQL) - requires: pip install psycopg[binary]
import os
import sys
import psycopg

def normalize_username(username: str) -> str:
    if username is None:
        return ""
    return username.strip().casefold()

def username_exists(username: str) -> bool:
    u = normalize_username(username)
    if not u:
        return False

    dsn = os.getenv("DB_DSN")  # e.g., "host=... dbname=... user=app_readonly password=... sslmode=require"
    if not dsn:
        return False

    sql = "SELECT 1 FROM users WHERE username = %s LIMIT 1"

    try:
        with psycopg.connect(dsn, connect_timeout=5) as conn:
            conn.autocommit = True
            with conn.cursor() as cur:
                cur.execute(sql, (u,))
                return cur.fetchone() is not None
    except Exception:
        # Do not leak DB/SQL details
        return False

if __name__ == "__main__":
    user = sys.argv[1] if len(sys.argv) > 1 else ""
    print("true" if username_exists(user) else "false")