# app.py
from flask import Flask, request, jsonify
import os
import psycopg2
from psycopg2.extras import RealDictCursor

app = Flask(__name__)

def get_conn():
    return psycopg2.connect(
        host=os.getenv("DB_HOST", "localhost"),
        port=int(os.getenv("DB_PORT", "5432")),
        dbname=os.getenv("DB_NAME", "mydb"),
        user=os.getenv("DB_USER", "myuser"),
        password=os.getenv("DB_PASSWORD", "mypassword"),
    )

@app.get("/is_moderator/<string:username>")
def is_moderator(username: str):
    # Connect to the database
    conn = get_conn()
    try:
        with conn.cursor(cursor_factory=RealDictCursor) as cur:
            # Execute a query to check if the user is a moderator
            cur.execute(
                """
                SELECT EXISTS (
                    SELECT 1
                    FROM users
                    WHERE username = %s AND is_moderator = TRUE
                ) AS is_moderator;
                """,
                (username,),
            )
            row = cur.fetchone()
            return jsonify({"username": username, "is_moderator": bool(row["is_moderator"])})
    finally:
        conn.close()

if __name__ == "__main__":
    app.run(debug=True)
