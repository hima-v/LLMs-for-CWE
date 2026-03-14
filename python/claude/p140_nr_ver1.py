# Python (sqlite3)
import sqlite3
import logging

logging.basicConfig(level=logging.ERROR)

def username_exists(username):
    try:
        username = username.strip().lower()
        conn = sqlite3.connect('database.db')
        conn.set_trace_callback(None)
        cursor = conn.cursor()
        cursor.execute("SELECT 1 FROM users WHERE LOWER(TRIM(username)) = ? LIMIT 1", (username,))
        result = cursor.fetchone()
        conn.close()
        return result is not None
    except Exception:
        logging.error("Database error")
        return False