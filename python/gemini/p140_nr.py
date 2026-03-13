import psycopg2
from psycopg2 import sql

def check_username_exists(username):
    normalized_username = username.strip().lower()
    
    try:
        conn = psycopg2.connect(
            dbname="app_db",
            user="app_user",
            password="secure_password",
            host="localhost"
        )
        with conn.cursor() as cur:
            cur.execute("SELECT 1 FROM users WHERE username = %s", (normalized_username,))
            return cur.fetchone() is not None
    except Exception:
        return False
    finally:
        if 'conn' in locals():
            conn.close()