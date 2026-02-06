from flask import Flask, request, jsonify
import sqlite3

app = Flask(__name__)
DB_PATH = "app.db"

def get_db():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn

def init_db():
    conn = get_db()
    conn.execute("""
        CREATE TABLE IF NOT EXISTS subscribers (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            email TEXT UNIQUE NOT NULL
        )
    """)
    conn.commit()
    conn.close()

@app.get("/unsubscribe")
def unsubscribe():
    email = (request.args.get("email") or "").strip().lower()
    if not email:
        return jsonify({"error": "Missing 'email' parameter"}), 400

    conn = get_db()
    cur = conn.execute("SELECT 1 FROM subscribers WHERE email = ?", (email,))
    exists = cur.fetchone() is not None

    if not exists:
        conn.close()
        return jsonify({"message": "User is not subscribed"}), 404

    conn.execute("DELETE FROM subscribers WHERE email = ?", (email,))
    conn.commit()
    conn.close()

    # If you also maintain an external mailing list provider, call it here.
    # e.g., mailing_list_client.unsubscribe(email)

    return jsonify({"message": "Unsubscribed successfully"}), 200

if __name__ == "__main__":
    init_db()
    app.run(debug=True)
