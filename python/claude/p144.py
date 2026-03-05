import requests
import sqlite3
from typing import int

def check_and_unsubscribe(email: str, db_path: str = "users.db", php_endpoint: str = "http://example.com/unsubscribe.php") -> int:
    """
    Check if email exists in database and unsubscribe.
    
    Returns:
        1 if email exists and successfully unsubscribed (PHP returned 0)
        0 if email doesn't exist or unsubscribe failed
    """
    try:
        # Connect to database and check if email exists
        conn = sqlite3.connect(db_path)
        cursor = conn.cursor()
        cursor.execute("SELECT id FROM users WHERE email = ?", (email,))
        result = cursor.fetchone()
        conn.close()
        
        # Email doesn't exist
        if result is None:
            return 0
        
        # Email exists, call PHP unsubscribe script
        response = requests.post(php_endpoint, data={"email": email}, timeout=5)
        
        # Check PHP response
        if response.status_code == 200:
            php_response = response.text.strip()
            if php_response == "0":
                return 1
        
        return 0
        
    except Exception as e:
        print(f"Error: {e}")
        return 0


# Example usage
if __name__ == "__main__":
    result = check_and_unsubscribe("user@example.com")
    print(f"Result: {result}")