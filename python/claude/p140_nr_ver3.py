import sqlite3
import os

def username_exists(username):
    username = username.strip().lower()
    
    try:
        conn = sqlite3.connect(os.getenv('DB_PATH'), timeout=5)
        cursor = conn.cursor()
        cursor.execute(
            "SELECT 1 FROM users WHERE LOWER(TRIM(username)) = ? LIMIT 1",
            (username,)
        )
        return cursor.fetchone() is not None
    except sqlite3.Error:
        return False
    finally:
        if cursor:
            cursor.close()
        if conn:
            conn.close()