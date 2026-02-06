from flask import Flask, request, redirect, url_for, render_template_string, g
import sqlite3

app = Flask(__name__)
DB_PATH = "app.db"

def get_db():
    if "db" not in g:
        g.db = sqlite3.connect(DB_PATH)
        g.db.row_factory = sqlite3.Row
    return g.db

@app.teardown_appcontext
def close_db(_):
    db = g.pop("db", None)
    if db:
        db.close()

def init_db():
    db = sqlite3.connect(DB_PATH)
    db.execute("""
        CREATE TABLE IF NOT EXISTS messages (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT NOT NULL,
            message TEXT NOT NULL,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP
        )
    """)
    db.commit()
    db.close()

def insert_user_message_in_db(username: str, message: str) -> None:
    db = get_db()
    db.execute("INSERT INTO messages (username, message) VALUES (?, ?)", (username, message))
    db.commit()

def fetch_messages():
    db = get_db()
    return db.execute(
        "SELECT username, message, created_at FROM messages ORDER BY id DESC LIMIT 50"
    ).fetchall()

MAIN_PAGE_HTML = """
<!doctype html>
<html>
  <head><meta charset="utf-8"><title>Messages</title></head>
  <body>
    <h2>Post a message</h2>
    <form method="post" action="{{ url_for('post') }}">
      <input name="username" placeholder="username" required>
      <input name="message" placeholder="message" required>
      <button type="submit">Send</button>
    </form>

    <h2>Recent messages</h2>
    <ul>
      {% for m in messages %}
        <li>
          <strong>{{ m['username'] }}</strong>: {{ m['message'] }}
          <small>({{ m['created_at'] }})</small>
        </li>
      {% else %}
        <li>No messages yet.</li>
      {% endfor %}
    </ul>
  </body>
</html>
"""

@app.get("/")
def index():
    return render_template_string(MAIN_PAGE_HTML, messages=fetch_messages())

@app.post("/post")
def post():
    username = (request.form.get("username") or "").strip()
    message = (request.form.get("message") or "").strip()

    if username and message:
        insert_user_message_in_db(username, message)

    return redirect(url_for("index"))

if __name__ == "__main__":
    init_db()
    app.run(debug=True)
