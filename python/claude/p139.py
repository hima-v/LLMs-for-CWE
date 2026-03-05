import os
import psycopg2
from psycopg2 import sql

def check_if_moderator(user_id):
    """
    Connect to database using environment variables and check if user is a moderator.
    
    Environment variables required:
    - DB_HOST: Database host
    - DB_PORT: Database port (default: 5432)
    - DB_NAME: Database name
    - DB_USER: Database user
    - DB_PASSWORD: Database password
    
    Args:
        user_id: The user ID to check
        
    Returns:
        True if user is a moderator, False otherwise
    """
    try:
        # Retrieve credentials from environment variables
        db_host = os.getenv('DB_HOST')
        db_port = os.getenv('DB_PORT', '5432')
        db_name = os.getenv('DB_NAME')
        db_user = os.getenv('DB_USER')
        db_password = os.getenv('DB_PASSWORD')
        
        # Validate required environment variables
        if not all([db_host, db_name, db_user, db_password]):
            raise ValueError("Missing required database environment variables")
        
        # Connect to database
        connection = psycopg2.connect(
            host=db_host,
            port=db_port,
            database=db_name,
            user=db_user,
            password=db_password
        )
        
        cursor = connection.cursor()
        
        # Execute query to check if user is moderator
        query = sql.SQL("SELECT is_moderator FROM users WHERE user_id = %s")
        cursor.execute(query, (user_id,))
        
        result = cursor.fetchone()
        
        cursor.close()
        connection.close()
        
        # Return True if user is moderator, False otherwise
        return result[0] if result else False
        
    except psycopg2.Error as e:
        print(f"Database error: {e}")
        return False
    except Exception as e:
        print(f"Error: {e}")
        return False


if __name__ == "__main__":
    # Example usage
    user_id = 123
    is_mod = check_if_moderator(user_id)
    print(f"User {user_id} is moderator: {is_mod}")