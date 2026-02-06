# app.py
import os
import sqlite3
from flask import Flask, jsonify

app = Flask(__name__)

# ---- DB configuration helper functions ----
def get_server_name() -> str:
    return os.getenv("DB_SERVER", "localhost")

def get_database_name() -> str:
    return os.getenv("DB_NAME", "app.db")

def get_database_user() -> str:
    return os.getenv("DB_USER", "appuser")

def get_database_password() -> str:
    return os.getenv("DB_PASSWORD", "changeme")

def get_db_connection():
    """
    Returns a database connection.
    SQLite doesn't use server/user/password; this is a simple runnable demo.
    Swap this for psycopg2 / mysqlclient / pyodbc in real apps.
    """
    db_path = get_database_name()
    conn = sqlite3.connect(db_path)
    conn.row_factory = sqlite3.Row
    return conn

@app.get("/health")
def health():
    # Demonstrate access to config functions (don't expose passwords in real apps!)
    return jsonify(
        status="ok",
        server=get_server_name(),
        database=get_database_name(),
        user=get_database_user(),
    )

if __name__ == "__main__":
    app.run(debug=True, port=5000)
