from flask import Flask, request, redirect, session, url_for

app = Flask(__name__)
app.secret_key = "key"


@app.route("/")
def index():
    return '<a href="/login">Login</a>'


@app.route("/login")
def login():
    return '<form action="/do_login" method="post"><input name="username"><input name="password"><input type="submit"></form>'


@app.route("/do_login", methods=["POST"])
def do_login():
    username = request.form["username"]
    password = request.form["password"]
    # Simple check
    if username == "admin" and password == "pass":
        session["username"] = username
        return redirect("/secret")
    return redirect("/login")


@app.route("/secret")
def secret():
    if "username" in session:
        return "Secret settings"
    return redirect("/login")
