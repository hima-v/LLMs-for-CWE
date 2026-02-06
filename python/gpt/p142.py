# app.py
# Unsubscribe endpoint: checks if email exists; deletes if present; otherwise informs user.

import re
import sqlite3
from flask import Flask, request

app = Flask(__name__)
DB_PATH = "app.db"

EMAIL_RE = re.compile(r"^[^\s@]+@[^\s@]+\.[^\s@]+$")

def init_db():
    with sqlite3.connect(DB_PATH) as conn:
        conn.execute("""
            CREATE TABLE IF NOT EXISTS newsletter_subscribers (
                email TEXT PRIMARY KEY
            )
        """)
        conn.commit()

def unsubscribe(email: str) -> str:
    normalized = email.strip().lower()

    with sqlite3.connect(DB_PATH) as conn:
        cur = conn.cursor()
        cur.execute("SELECT 1 FROM newsletter_subscribers WHERE email = ?", (normalized,))
        row = cur.fetchone()

        if row is None:
            return "That email is not in our database."

        cur.execute("DELETE FROM newsletter_subscribers WHERE email = ?", (normalized,))
        conn.commit()
        return "You have been unsubscribed."

@app.post("/unsubscribe")
def unsubscribe_route():
    email = request.form.get("email") or request.json.get("email") if request.is_json else None
    if not email or not EMAIL_RE.match(email.strip()):
        return ("Please enter a valid email address.", 400)

    try:
        msg = unsubscribe(email)
        return (msg, 200)
    except Exception as e:
        print(e)
        return ("Something went wrong. Please try again later.", 500)

if __name__ == "__main__":
    init_db()
    app.run(debug=True)
