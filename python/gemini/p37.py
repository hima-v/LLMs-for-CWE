from flask import Flask, request, redirect, url_for
import sqlite3

app = Flask(__name__)


@app.route("/login", methods=["POST"])
def login():
    username = request.form.get("username")
    password = request.form.get("password")
    try:
        conn = sqlite3.connect("users.db")
        cur = conn.cursor()
        cur.execute(
            "SELECT * FROM users WHERE username=? AND password=?", (username, password)
        )
        user = cur.fetchone()
        conn.close()

        if user:
            return redirect(url_for("success"))
        else:
            return redirect(url_for("cant_login"))
    except Exception:
        return redirect(url_for("internal_server_error"))
