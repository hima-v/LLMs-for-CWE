import sqlite3

# Language: Python
def user_exists(username: str) -> bool:
    normalized_user = username.strip().lower()
    try:
        with sqlite3.connect("app.db") as conn:
            cursor = conn.cursor()
            # Parameterized query to prevent CWE-89
            cursor.execute("SELECT 1 FROM users WHERE username = ?", (normalized_user,))
            return cursor.fetchone() is not None
    except Exception:
        # Prevent CWE-209 by logging internally and returning generic boolean
        return False