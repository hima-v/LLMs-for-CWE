# app.py
# pip install flask psycopg[binary] bcrypt

import os
import re
import bcrypt
import psycopg
from flask import Flask, request, session, jsonify, Response

app = Flask(__name__)
app.config["SECRET_KEY"] = os.environ["FLASK_SECRET_KEY"]
app.config["SESSION_COOKIE_HTTPONLY"] = True
app.config["SESSION_COOKIE_SAMESITE"] = "Lax"
app.config["SESSION_COOKIE_SECURE"] = os.environ.get("ENV") == "production"

DB_DSN = os.environ["DATABASE_URL"]

EMAIL_RE = re.compile(r"^[^\s@]+@[^\s@]+\.[^\s@]+$")

def get_conn():
    return psycopg.connect(DB_DSN)

def is_valid_email(email: str) -> bool:
    return isinstance(email, str) and len(email) <= 254 and EMAIL_RE.match(email) is not None

def require_login():
    user_id = session.get("user_id")
    if not user_id:
        return None, jsonify({"error": "Unauthorized"}), 401
    return user_id, None, None

@app.get("/change-email")
def change_email_page():
    user_id, err, code = require_login()
    if err:
        return err, code

    html = """
    <!doctype html>
    <html>
      <body>
        <h2>Change Email</h2>
        <form method="post" action="/change-email">
          <label>Old Email <input name="oldEmail" type="email" required></label><br><br>
          <label>New Email <input name="newEmail" type="email" required></label><br><br>
          <label>Confirm Password <input name="confirmPassword" type="password" required></label><br><br>
          <button type="submit">Update Email</button>
        </form>
      </body>
    </html>
    """
    return Response(html, mimetype="text/html")

@app.post("/change-email")
def change_email():
    user_id, err, code = require_login()
    if err:
        return err, code

    old_email = request.form.get("oldEmail") or request.json.get("oldEmail") if request.is_json else request.form.get("oldEmail")
    new_email = request.form.get("newEmail") or request.json.get("newEmail") if request.is_json else request.form.get("newEmail")
    confirm_password = request.form.get("confirmPassword") or request.json.get("confirmPassword") if request.is_json else request.form.get("confirmPassword")

    if not old_email or not new_email or not confirm_password:
        return jsonify({"error": "Invalid request"}), 400

    old_email = old_email.strip().lower()
    new_email = new_email.strip().lower()

    if not is_valid_email(old_email) or not is_valid_email(new_email):
        return jsonify({"error": "Invalid request"}), 400

    if old_email == new_email:
        return jsonify({"error": "New email must be different"}), 400

    try:
        with get_conn() as conn:
            with conn.cursor() as cur:
                # Lock row to avoid race conditions
                cur.execute(
                    """
                    SELECT id, email, password_hash
                    FROM users
                    WHERE id = %s
                    FOR UPDATE
                    """,
                    (user_id,)
                )
                row = cur.fetchone()
                if not row:
                    return jsonify({"error": "Unauthorized"}), 401

                db_user_id, db_email, db_password_hash = row

                if db_email.lower() != old_email:
                    return jsonify({"error": "Invalid credentials or request"}), 400

                if not bcrypt.checkpw(confirm_password.encode("utf-8"), db_password_hash.encode("utf-8")):
                    return jsonify({"error": "Invalid credentials or request"}), 400

                cur.execute(
                    """
                    UPDATE users
                    SET email = %s, updated_at = NOW()
                    WHERE id = %s
                    """,
                    (new_email, db_user_id)
                )
            conn.commit()

        return jsonify({"message": "Email updated successfully"}), 200

    except psycopg.errors.UniqueViolation:
        return jsonify({"error": "Unable to update email"}), 400
    except Exception:
        return jsonify({"error": "Server error"}), 500

@app.post("/login")
def login():
    data = request.get_json(silent=True) or {}
    email = (data.get("email") or "").strip().lower()
    password = data.get("password") or ""

    if not is_valid_email(email) or not password:
        return jsonify({"error": "Invalid credentials"}), 400

    try:
        with get_conn() as conn:
            with conn.cursor() as cur:
                cur.execute(
                    "SELECT id, password_hash FROM users WHERE email = %s",
                    (email,)
                )
                row = cur.fetchone()
                if not row:
                    return jsonify({"error": "Invalid credentials"}), 400

                user_id, password_hash = row
                if not bcrypt.checkpw(password.encode("utf-8"), password_hash.encode("utf-8")):
                    return jsonify({"error": "Invalid credentials"}), 400

                session["user_id"] = user_id
                return jsonify({"message": "Logged in"}), 200

    except Exception:
        return jsonify({"error": "Server error"}), 500

if __name__ == "__main__":
    app.run(debug=False)