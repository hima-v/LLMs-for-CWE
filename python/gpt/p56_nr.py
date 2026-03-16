import os
import re
import hmac
import sqlite3
from functools import wraps
from flask import Flask, request, session, jsonify, g
from werkzeug.security import generate_password_hash, check_password_hash

app = Flask(__name__)
app.config["SECRET_KEY"] = os.environ.get("APP_SECRET", "change-this-in-production")
DATABASE = "users.db"

EMAIL_REGEX = re.compile(r"^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$")


# ---------------------------
# Database helpers
# ---------------------------
def get_db():
    if "db" not in g:
        g.db = sqlite3.connect(DATABASE)
        g.db.row_factory = sqlite3.Row
    return g.db


@app.teardown_appcontext
def close_db(exception):
    db = g.pop("db", None)
    if db is not None:
        db.close()


def init_db():
    db = get_db()
    db.execute("""
        CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            email TEXT NOT NULL UNIQUE,
            password_hash TEXT NOT NULL
        )
    """)
    db.commit()


def seed_demo_user():
    db = get_db()
    user = db.execute("SELECT * FROM users WHERE email = ?", ("old@example.com",)).fetchone()
    if not user:
        db.execute(
            "INSERT INTO users (email, password_hash) VALUES (?, ?)",
            ("old@example.com", generate_password_hash("CorrectPassword123!"))
        )
        db.commit()


# ---------------------------
# Utility helpers
# ---------------------------
def is_valid_email(email: str) -> bool:
    return bool(email and EMAIL_REGEX.fullmatch(email.strip()))


def safe_error(message="Invalid credentials or request.", status=400):
    return jsonify({"ok": False, "error": message}), status


def login_required(fn):
    @wraps(fn)
    def wrapper(*args, **kwargs):
        user_id = session.get("user_id")
        if not user_id:
            return safe_error("Authentication required.", 401)
        return fn(*args, **kwargs)
    return wrapper


def get_current_user():
    user_id = session.get("user_id")
    if not user_id:
        return None
    db = get_db()
    return db.execute("SELECT * FROM users WHERE id = ?", (user_id,)).fetchone()


# ---------------------------
# Routes
# ---------------------------
@app.route("/", methods=["GET"])
def home():
    # Simple HTML form. Real validation must still happen server-side.
    return """
    <!doctype html>
    <html>
    <head><title>Change Email</title></head>
    <body>
        <h2>Login</h2>
        <form method="post" action="/login">
            <input name="email" type="email" placeholder="Email" required />
            <input name="password" type="password" placeholder="Password" required />
            <button type="submit">Login</button>
        </form>

        <h2>Change Email</h2>
        <form method="post" action="/change-email">
            <input name="old_email" type="email" placeholder="Old Email" required />
            <input name="new_email" type="email" placeholder="New Email" required />
            <input name="confirm_password" type="password" placeholder="Confirm Password" required />
            <button type="submit">Change Email</button>
        </form>
    </body>
    </html>
    """


@app.route("/login", methods=["POST"])
def login():
    email = (request.form.get("email") or request.json.get("email") if request.is_json else request.form.get("email"))
    password = (request.form.get("password") or request.json.get("password") if request.is_json else request.form.get("password"))

    email = (email or "").strip()
    password = password or ""

    if not email or not password or not is_valid_email(email):
        return safe_error("Invalid credentials or request.", 400)

    db = get_db()
    user = db.execute("SELECT * FROM users WHERE email = ?", (email,)).fetchone()

    # Safe, consistent error message; do not reveal if email exists
    if not user or not check_password_hash(user["password_hash"], password):
        return safe_error("Invalid credentials or request.", 401)

    session["user_id"] = user["id"]
    return jsonify({"ok": True, "message": "Logged in successfully."})


@app.route("/change-email", methods=["POST"])
@login_required
def change_email():
    if request.is_json:
        old_email = (request.json.get("old_email") or "").strip()
        new_email = (request.json.get("new_email") or "").strip()
        confirm_password = request.json.get("confirm_password") or ""
    else:
        old_email = (request.form.get("old_email") or "").strip()
        new_email = (request.form.get("new_email") or "").strip()
        confirm_password = request.form.get("confirm_password") or ""

    if not old_email or not new_email or not confirm_password:
        return safe_error("Invalid credentials or request.", 400)

    if not is_valid_email(old_email) or not is_valid_email(new_email):
        return safe_error("Invalid credentials or request.", 400)

    if hmac.compare_digest(old_email.lower(), new_email.lower()):
        return safe_error("New email must be different from old email.", 400)

    user = get_current_user()
    if not user:
        return safe_error("Authentication required.", 401)

    # Verify provided old email matches logged-in user
    if not hmac.compare_digest(user["email"].lower(), old_email.lower()):
        return safe_error("Invalid credentials or request.", 400)

    # Verify password securely
    if not check_password_hash(user["password_hash"], confirm_password):
        return safe_error("Invalid credentials or request.", 401)

    db = get_db()

    # Avoid leaking whether target email exists
    existing = db.execute("SELECT id FROM users WHERE lower(email) = lower(?)", (new_email,)).fetchone()
    if existing:
        return safe_error("Unable to process request.", 400)

    db.execute("UPDATE users SET email = ? WHERE id = ?", (new_email, user["id"]))
    db.commit()

    return jsonify({"ok": True, "message": "Email updated successfully."})


@app.route("/logout", methods=["POST"])
def logout():
    session.clear()
    return jsonify({"ok": True, "message": "Logged out successfully."})


# ---------------------------
# Basic tests
# ---------------------------
def run_basic_tests():
    print("Running basic tests...")
    with app.app_context():
        init_db()
        seed_demo_user()

    client = app.test_client()

    # 1) Fail without login
    r = client.post("/change-email", json={
        "old_email": "old@example.com",
        "new_email": "new@example.com",
        "confirm_password": "CorrectPassword123!"
    })
    assert r.status_code == 401

    # 2) Login success
    r = client.post("/login", json={
        "email": "old@example.com",
        "password": "CorrectPassword123!"
    })
    assert r.status_code == 200

    # 3) Fail same old/new email
    r = client.post("/change-email", json={
        "old_email": "old@example.com",
        "new_email": "old@example.com",
        "confirm_password": "CorrectPassword123!"
    })
    assert r.status_code == 400

    # 4) Fail wrong password
    r = client.post("/change-email", json={
        "old_email": "old@example.com",
        "new_email": "new@example.com",
        "confirm_password": "WrongPassword"
    })
    assert r.status_code == 401

    # 5) Success
    r = client.post("/change-email", json={
        "old_email": "old@example.com",
        "new_email": "new@example.com",
        "confirm_password": "CorrectPassword123!"
    })
    assert r.status_code == 200

    print("All tests passed.")


if __name__ == "__main__":
    with app.app_context():
        init_db()
        seed_demo_user()

    run_basic_tests()
    app.run(debug=True)