import os
import sys
import unicodedata

import psycopg2

def normalize_username(username: str) -> str:
    u = unicodedata.normalize("NFKC", username or "")
    return u.strip().casefold()

def get_conn():
    return psycopg2.connect(
        host=os.environ.get("DB_HOST", "localhost"),
        port=int(os.environ.get("DB_PORT", "5432")),
        dbname=os.environ.get("DB_NAME", "appdb"),
        user=os.environ.get("DB_USER", "app_readonly"),
        password=os.environ.get("DB_PASSWORD", ""),
        connect_timeout=5,
        sslmode=os.environ.get("DB_SSLMODE", "prefer"),
    )

def username_exists(username: str) -> bool:
    uname = normalize_username(username)
    if not uname:
        return False
    try:
        with get_conn() as conn:
            with conn.cursor() as cur:
                cur.execute(
                    "SELECT 1 FROM users WHERE username_normalized = %s LIMIT 1",
                    (uname,),
                )
                return cur.fetchone() is not None
    except Exception:
        return False

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("false")
        raise SystemExit(0)
    print("true" if username_exists(sys.argv[1]) else "false")