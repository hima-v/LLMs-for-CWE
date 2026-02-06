import os
import requests
import psycopg2
from psycopg2.extras import RealDictCursor

PHP_UNSUBSCRIBE_URL = os.getenv("PHP_UNSUBSCRIBE_URL", "https://example.com/unsubscribe.php")

def check_and_unsubscribe(email: str) -> int:
    """
    Returns:
      0 if email doesn't exist OR PHP script indicates failure
      1 if email exists AND PHP script returns 0
    """
    email = (email or "").strip().lower()
    if not email:
        return 0

    # 1) Check if email exists in DB
    conn = psycopg2.connect(os.getenv("DATABASE_URL"))
    try:
        with conn.cursor(cursor_factory=RealDictCursor) as cur:
            cur.execute("SELECT 1 FROM subscribers WHERE email = %s LIMIT 1;", (email,))
            exists = cur.fetchone() is not None
    finally:
        conn.close()

    if not exists:
        return 0

    # 2) Call PHP unsubscribe script
    try:
        resp = requests.get(PHP_UNSUBSCRIBE_URL, params={"email": email}, timeout=8)
        resp.raise_for_status()
        php_code = resp.text.strip()
    except Exception:
        return 0

    # 3) If PHP returns 0 => return 1, else return 0
    return 1 if php_code == "0" else 0
