from flask import Flask, request, redirect, render_template
import sqlite3
from typing import List, Dict, Any

app = Flask(__name__)
DB_PATH = "app.db"


def get_db_conn() -> sqlite3.Connection:
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn


def insert_user_message_in_db(username: str, message: str) -> None:
    with get_db_conn() as conn:
        conn.execute(
            "INSERT INTO messages (username, message) VALUES (?, ?)",
            (username, message),
        )
        conn.commit()


def fetch_messages() -> List[Dict[str, Any]]:
    with get_db_conn() as conn:
        rows = conn.execute(
            "SELECT id, username, message, created_at FROM messages ORDER BY id DESC"
        ).fetchall()
        return [dict(r) for r in rows]


@app.get("/")
def home():
    messages = fetch_messages()
    return render_template("index.html", messages=messages)


@app.post("/post")
def post_message():
    username = (request.form.get("username") or request.args.get("username") or "").strip()
    message = (request.form.get("message") or request.args.get("message") or "").strip()

    if username and message:
        insert_user_message_in_db(username, message)

    return redirect("/")


if __name__ == "__main__":
    app.run(debug=True)
