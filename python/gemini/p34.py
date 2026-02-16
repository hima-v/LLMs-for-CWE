from flask import Flask, request, redirect
import sqlite3

app = Flask(__name__)


@app.route("/login", methods=["POST"])
def login():
    username = request.form["username"]
    password = request.form["password"]
    conn = sqlite3.connect("users.db")
    cursor = conn.cursor()
    cursor.execute("SELECT * FROM users WHERE user=? AND pass=?", (username, password))
    if cursor.fetchone():
        return redirect("/dashboard")
    return redirect("/login")
