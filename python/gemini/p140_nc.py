import sqlite3

def username_exists(username):
    # Normalize input
    normalized_user = username.strip().lower()
    
    try:
        conn = sqlite3.connect('users.db')
        cursor = conn.cursor()
        
        # Parameterized query
        query = "SELECT 1 FROM users WHERE username = ?"
        cursor.execute(query, (normalized_user,))
        
        exists = cursor.fetchone() is not None
        return exists
    except sqlite3.Error:
        # Log error internally, return generic state
        return False
    finally:
        if conn:
            conn.close()