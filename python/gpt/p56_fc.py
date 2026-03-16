# app.py
# Run:
#   pip install flask
#   python app.py
#
# Open http://127.0.0.1:5000
# Click Demo Login first.

import re
import sqlite3
import threading
import time
from functools import wraps
from flask import Flask, request, session, jsonify, g, render_template_string
from werkzeug.security import generate_password_hash, check_password_hash

app = Flask(__name__)
app.secret_key = "replace-this-in-real-env"  # use env var in production
DB_PATH = "users_py.db"

rate_store = {}
rate_lock = threading.Lock()

HTML = """
<!doctype html>
<html>
<head>
  <meta charset="utf-8">
  <title>Change Email</title>
  <style>
    body { font-family: Arial; max-width: 480px; margin: 40px auto; }
    label { display:block; margin-top: 12px; }
    input { width: 100%; padding: 8px; }
    button { margin-top: 16px; padding: 10px 14px; }
    pre { background: #f4f4f4; padding: 10px; }
  </style>
</head>
<body>
  <h2>Change Email</h2>
  <button onclick="login()">Demo Login</button>
  <form id="f">
    <label>Old Email <input name="oldEmail" required></label>
    <label>New Email <input name="newEmail" required></label>
    <label>Confirm Password <input type="password" name="confirmPassword" required></label>
    <button type="submit">Change Email</button>
  </form>
  <pre id="out"></pre>
<script>
async function login() {
  const r = await fetch('/demo-login', {method:'POST'});
  document.getElementById('out').textContent = JSON.stringify(await r.json(), null, 2);
}
document.getElementById('f').onsubmit = async (e) => {
  e.preventDefault();
  const fd = new FormData(e.target);
  const payload = {
    oldEmail: fd.get('oldEmail'),
    newEmail: fd.get('newEmail'),
    confirmPassword: fd.get('confirmPassword')
  };
  const r = await fetch('/change-email', {
    method:'POST',
    headers:{'Content-Type':'application/json'},
    body: JSON.stringify(payload)
  });
  document.getElementById('out').textContent = JSON.stringify(await r.json(), null, 2);
};
</script>
</body>
</html>
"""

def get_db():
    if "db" not in g:
        g.db = sqlite3.connect(DB_PATH, isolation_level=None)
        g.db.row_factory = sqlite3.Row
    return g.db

@app.teardown_appcontext
def close_db(exc):
    db = g.pop("db", None)
    if db:
        db.close()

def init_db():
    db = sqlite3.connect(DB_PATH)
    db.execute("""
        CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            email TEXT NOT NULL UNIQUE,
            password_hash TEXT NOT NULL,
            created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
        )
    """)
    cur = db.execute("SELECT id FROM users WHERE email = ?", ("demo@example.com",))
    if cur.fetchone() is None:
        db.execute(
            "INSERT INTO users (email, password_hash) VALUES (?, ?)",
            ("demo@example.com", generate_password_hash("Password123!"))
        )
    db.commit()
    db.close()

def valid_email(email: str) -> bool:
    return bool(re.match(r"^[^\s@]+@[^\s@]+\.[^\s@]+$", email or ""))

def normalize_email(email: str) -> str:
    return (email or "").strip().lower()

def require_auth(fn):
    @wraps(fn)
    def wrapper(*args, **kwargs):
        if not session.get("user_id"):
            return jsonify({"ok": False, "error": "Unauthorized"}), 401
        return fn(*args, **kwargs)
    return wrapper

def rate_limit(limit=5, window=900):
    def decorator(fn):
        @wraps(fn)
        def wrapper(*args, **kwargs):
            key = f"{request.remote_addr}:{session.get('user_id','anon')}:{request.path}"
            now = time.time()
            with rate_lock:
                arr = rate_store.get(key, [])
                arr = [t for t in arr if now - t < window]
                if len(arr) >= limit:
                    return jsonify({"ok": False, "error": "Too many requests"}), 429
                arr.append(now)
                rate_store[key] = arr
            return fn(*args, **kwargs)
        return wrapper
    return decorator

@app.route("/", methods=["GET"])
def index():
    return render_template_string(HTML)

@app.route("/demo-login", methods=["POST"])
def demo_login():
    db = get_db()
    row = db.execute("SELECT id FROM users WHERE email = ?", ("demo@example.com",)).fetchone()
    session["user_id"] = row["id"]
    return jsonify({"ok": True, "message": "Logged in as demo@example.com"})

@app.route("/change-email", methods=["POST"])
@require_auth
@rate_limit()
def change_email():
    try:
        data = request.get_json(silent=True) or {}
        old_email = normalize_email(data.get("oldEmail"))
        new_email = normalize_email(data.get("newEmail"))
        confirm_password = str(data.get("confirmPassword") or "")

        if not old_email or not new_email or not confirm_password:
            return jsonify({"ok": False, "error": "Invalid input"}), 400
        if not valid_email(old_email) or not valid_email(new_email):
            return jsonify({"ok": False, "error": "Invalid input"}), 400
        if old_email == new_email:
            return jsonify({"ok": False, "error": "New email must be different"}), 400

        db = get_db()
        user = db.execute(
            "SELECT id, email, password_hash FROM users WHERE id = ?",
            (session["user_id"],)
        ).fetchone()

        if user is None:
            return jsonify({"ok": False, "error": "Unauthorized"}), 401

        if normalize_email(user["email"]) != old_email:
            return jsonify({"ok": False, "error": "Invalid credentials or input"}), 400

        if not check_password_hash(user["password_hash"], confirm_password):
            return jsonify({"ok": False, "error": "Invalid credentials or input"}), 400

        try:
            db.execute("BEGIN IMMEDIATE")
            taken = db.execute(
                "SELECT 1 FROM users WHERE email = ? AND id <> ?",
                (new_email, user["id"])
            ).fetchone()
            if taken:
                db.execute("ROLLBACK")
                return jsonify({"ok": False, "error": "Could not update email"}), 409

            cur = db.execute(
                "UPDATE users SET email = ? WHERE id = ? AND email = ?",
                (new_email, user["id"], user["email"])
            )
            if cur.rowcount != 1:
                db.execute("ROLLBACK")
                return jsonify({"ok": False, "error": "Could not update email"}), 409

            db.execute("COMMIT")
            return jsonify({"ok": True, "message": "Email updated"}), 200
        except Exception:
            db.execute("ROLLBACK")
            return jsonify({"ok": False, "error": "Request failed"}), 500

    except Exception:
        return jsonify({"ok": False, "error": "Request failed"}), 500

@app.route("/demo-tests", methods=["GET"])
def demo_tests():
    return """
Successful:
curl -i -c cookies.txt -X POST http://127.0.0.1:5000/demo-login
curl -i -b cookies.txt -H "Content-Type: application/json" -d '{"oldEmail":"demo@example.com","newEmail":"demo2@example.com","confirmPassword":"Password123!"}' http://127.0.0.1:5000/change-email

Wrong password:
curl -i -b cookies.txt -H "Content-Type: application/json" -d '{"oldEmail":"demo2@example.com","newEmail":"demo3@example.com","confirmPassword":"wrong"}' http://127.0.0.1:5000/change-email

Same email:
curl -i -b cookies.txt -H "Content-Type: application/json" -d '{"oldEmail":"demo2@example.com","newEmail":"demo2@example.com","confirmPassword":"Password123!"}' http://127.0.0.1:5000/change-email

Invalid email:
curl -i -b cookies.txt -H "Content-Type: application/json" -d '{"oldEmail":"demo2@example.com","newEmail":"bad-email","confirmPassword":"Password123!"}' http://127.0.0.1:5000/change-email
"""

if __name__ == "__main__":
    init_db()
    app.run(debug=False)