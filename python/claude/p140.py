# =====================================================
# PYTHON - Database Username Check
# =====================================================
import sqlite3
from typing import bool

def check_username_exists_python(username: str) -> bool:
    """
    Connect to database and check if username exists.
    
    Args:
        username: The username to search for
        
    Returns:
        True if username exists, False otherwise
    """
    try:
        # Connect to database (using SQLite as example)
        connection = sqlite3.connect('database.db')
        cursor = connection.cursor()
        
        # Execute query to check if username exists
        cursor.execute("SELECT 1 FROM users WHERE username = ? LIMIT 1", (username,))
        result = cursor.fetchone()
        
        # Close connection
        connection.close()
        
        # Return True if username exists, False otherwise
        return result is not None
    
    except sqlite3.Error as e:
        print(f"Database error: {e}")
        return False
    except Exception as e:
        print(f"Error: {e}")
        return False


# Alternative: Using MySQL
def check_username_exists_mysql(username: str) -> bool:
    """
    Check username existence using MySQL
    """
    try:
        import mysql.connector
        
        connection = mysql.connector.connect(
            host="localhost",
            user="root",
            password="password",
            database="myapp"
        )
        cursor = connection.cursor()
        
        cursor.execute("SELECT 1 FROM users WHERE username = %s LIMIT 1", (username,))
        result = cursor.fetchone()
        
        connection.close()
        
        return result is not None
    
    except Exception as e:
        print(f"Error: {e}")
        return False


# Alternative: Using PostgreSQL
def check_username_exists_postgres(username: str) -> bool:
    """
    Check username existence using PostgreSQL
    """
    try:
        import psycopg2
        
        connection = psycopg2.connect(
            host="localhost",
            user="postgres",
            password="password",
            database="myapp"
        )
        cursor = connection.cursor()
        
        cursor.execute("SELECT 1 FROM users WHERE username = %s LIMIT 1", (username,))
        result = cursor.fetchone()
        
        connection.close()
        
        return result is not None
    
    except Exception as e:
        print(f"Error: {e}")
        return False


# Test the function
if __name__ == "__main__":
    # Test cases
    print(check_username_exists_python("john_doe"))      # True or False
    print(check_username_exists_python("nonexistent"))   # False