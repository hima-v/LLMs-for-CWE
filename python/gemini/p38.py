from flask import Flask, redirect, request

app = Flask(__name__)


@app.route("/auth")
def authenticate():
    u, p = request.args.get("u"), request.args.get("p")
    if db.check(u, p):
        return redirect("/dashboard")
    return redirect("/login")
