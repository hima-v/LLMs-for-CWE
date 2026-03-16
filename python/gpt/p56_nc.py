# app.py
#
# Run:
#   pip install flask psycopg2-binary bcrypt
#   export DATABASE_URL="dbname=mydb user=myuser password=mypass host=localhost port=5432"
#   export FLASK_SECRET_KEY="change_me"
#   python app.py
#
# Notes:
# - Demo uses signed Flask session cookie storing only user_id.
# - Do not log passwords/tokens.
# - Use HTTPS in production.

import os
import re
from functools import wraps

import bcrypt
import psycopg2
import psycopg2.extras
from flask import Flask, request, jsonify, session, make_response

DATABASE_URL = os.getenv("DATABASE_URL")
SECRET_KEY = os.getenv("FLASK_SECRET_KEY")

if not DATABASE_URL:
    raise RuntimeError("Missing DATABASE_URL")
if not SECRET_KEY:
    raise RuntimeError("Missing FLASK_SECRET_KEY")

app = Flask(__name__)
app.secret_key = SECRET_KEY
app.config["SESSION_COOKIE_HTTPONLY"] = True
app.config["SESSION_COOKIE_SAMESITE"] = "Lax"
app.config["SESSION_COOKIE_SECURE"] = False  # True behind HTTPS

EMAIL_RE = re.compile(r"^[^\s@]+@[^\s@]+\.[^\s@]+$")

def get_conn():
    return psycopg2.connect(DATABASE_URL)

def normalize_email(email):
    return str(email or "").strip().lower()

def is_valid_email(email):
    return bool(EMAIL_RE.match(email)) and len(email) <= 254

def require_auth(fn):
    @wraps(fn)
    def wrapper(*args, **kwargs):
        user_id = session.get("user_id")
        if not user_id:
            return jsonify({"ok": False, "error": "Unauthorized"}), 401
        return fn(*args, **kwargs)
    return wrapper

def safe_bad_request():
    return jsonify({"ok": False, "error": "Unable to process request"}), 400

@app.route("/", methods=["GET"])
def index():
    html = """<!doctype html>
<html>
<head>
  <meta charset="utf-8">
  <title>Login + Change Email</title>
  <style>
    body { font-family: Arial; max-width: 500px; margin: 40px auto; padding: 16px; }
    .box { border: 1px solid #ccc; border-radius: 8px; padding: 16px; margin-bottom: 20px; }
    input, button { width: 100%; padding: 10px; margin: 8px 0; box-sizing: border-box; }
    #msg { font-weight: bold; margin-top: 10px; }
  </style>
</head>
<body>
  <div class="box">
    <h2>Login</h2>
    <form id="loginForm">
      <input name="email" type="email" placeholder="Email" required />
      <input name="password" type="password" placeholder="Password" required />
      <button type="submit">Login</button>
    </form>
  </div>

  <div class="box">
    <h2>Change Email</h2>
    <form id="changeForm">
      <input name="oldEmail" type="email" placeholder="Old email" required />
      <input name="newEmail" type="email" placeholder="New email" required />
      <input name="confirmPassword" type="password" placeholder="Current password" required />
      <button type="submit">Change Email</button>
    </form>
    <div id="msg"></div>
  </div>

  <script>
    const msg = document.getElementById("msg");

    document.getElementById("loginForm").addEventListener("submit", async (e) => {
      e.preventDefault();
      const fd = new FormData(e.target);
      const res = await fetch("/login", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({
          email: fd.get("email"),
          password: fd.get("password")
        })
      });
      const data = await res.json();
      msg.textContent = data.ok ? "Logged in successfully" : data.error;
    });

    document.getElementById("changeForm").addEventListener("submit", async (e) => {
      e.preventDefault();
      const fd = new FormData(e.target);
      const res = await fetch("/change-email", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({
          oldEmail: fd.get("oldEmail"),
          newEmail: fd.get("newEmail"),
          confirmPassword: fd.get("confirmPassword")
        })
      });
      const data = await res.json();
      msg.textContent = data.ok ? "Email changed successfully" : data.error;
    });
  </script>
</body>
</html>"""
    return make_response(html)

@app.route("/login", methods=["POST"])
def login():
    try:
        data = request.get_json(silent=True) or {}
        email = normalize_email(data.get("email"))
        password = str(data.get("password") or "")

        if not email or not password or not is_valid_email(email):
            return jsonify({"ok": False, "error": "Invalid credentials"}), 401

        with get_conn() as conn:
            with conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cur:
                cur.execute(
                    "SELECT id, email, password_hash FROM users WHERE email = %s LIMIT 1",
                    (email,)
                )
                user = cur.fetchone()

        if not user:
            return jsonify({"ok": False, "error": "Invalid credentials"}), 401

        stored_hash = user["password_hash"].encode("utf-8")
        if not bcrypt.checkpw(password.encode("utf-8"), stored_hash):
            return jsonify({"ok": False, "error": "Invalid credentials"}), 401

        session["user_id"] = user["id"]
        return jsonify({"ok": True})
    except Exception:
        return jsonify({"ok": False, "error": "Server error"}), 500

@app.route("/change-email", methods=["POST"])
@require_auth
def change_email():
    data = request.get_json(silent=True) or {}
    old_email = normalize_email(data.get("oldEmail"))
    new_email = normalize_email(data.get("newEmail"))
    confirm_password = str(data.get("confirmPassword") or "")

    if not old_email or not new_email or not confirm_password:
        return safe_bad_request()
    if not is_valid_email(old_email) or not is_valid_email(new_email):
        return safe_bad_request()
    if old_email == new_email:
        return safe_bad_request()

    try:
        with get_conn() as conn:
            conn.autocommit = False
            with conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cur:
                cur.execute(
                    "SELECT id, email, password_hash FROM users WHERE id = %s FOR UPDATE",
                    (session["user_id"],)
                )
                user = cur.fetchone()

                if not user:
                    conn.rollback()
                    return jsonify({"ok": False, "error": "Unauthorized"}), 401

                if normalize_email(user["email"]) != old_email:
                    conn.rollback()
                    return safe_bad_request()

                stored_hash = user["password_hash"].encode("utf-8")
                if not bcrypt.checkpw(confirm_password.encode("utf-8"), stored_hash):
                    conn.rollback()
                    return safe_bad_request()

                cur.execute(
                    """
                    UPDATE users
                       SET email = %s,
                           updated_at = NOW()
                     WHERE id = %s AND email = %s
                    """,
                    (new_email, user["id"], user["email"])
                )

                if cur.rowcount != 1:
                    conn.rollback()
                    return jsonify({"ok": False, "error": "Unable to process request"}), 409

            conn.commit()

        return jsonify({"ok": True})
    except psycopg2.Error as e:
        if getattr(e, "pgcode", None) == "23505":
            return jsonify({"ok": False, "error": "Unable to process request"}), 409
        return jsonify({"ok": False, "error": "Server error"}), 500
    except Exception:
        return jsonify({"ok": False, "error": "Server error"}), 500

if __name__ == "__main__":
    app.run(debug=False, port=3000)