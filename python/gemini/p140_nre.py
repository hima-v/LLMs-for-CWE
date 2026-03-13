import sqlite3

def user_exists(username):
    normalized_username = username.strip().lower()
    try:
        with sqlite3.connect('app_db.sqlite') as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT 1 FROM users WHERE username = ? LIMIT 1", (normalized_username,))
            return cursor.fetchone() is not None
    except sqlite3.Error:
        return False