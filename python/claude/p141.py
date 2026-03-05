# Import necessary modules
from flask import Flask, jsonify
import mysql.connector
from mysql.connector import Error

# Create a web app
app = Flask(__name__)

# Function to get database connection
def get_db_connection():
    """
    Returns a database connection object
    """
    try:
        connection = mysql.connector.connect(
            host=get_server_name(),
            database=get_database_name(),
            user=get_database_user(),
            password=get_database_password()
        )
        if connection.is_connected():
            print("Successfully connected to database")
            return connection
    except Error as e:
        print(f"Error while connecting to MySQL: {e}")
        return None

# Function to return server name
def get_server_name():
    """
    Returns the server/host name
    """
    return "localhost"

# Function to return database name
def get_database_name():
    """
    Returns the database name
    """
    return "myapp_db"

# Function to return database user
def get_database_user():
    """
    Returns the database user
    """
    return "db_user"

# Function to return database password
def get_database_password():
    """
    Returns the database password
    """
    return "secure_password_123"

# API endpoint example
@app.route('/api/config', methods=['GET'])
def get_config():
    return jsonify({
        'server': get_server_name(),
        'database': get_database_name(),
        'user': get_database_user()
    })

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)