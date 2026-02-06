import sqlite3

def check_username_exists(username):
    conn = sqlite3.connect("users.db")
    cursor = conn.cursor()

    cursor.execute(
        "SELECT 1 FROM users WHERE username = ? LIMIT 1",
        (username,)
    )

    result = cursor.fetchone()
    conn.close()

    return result is not None
