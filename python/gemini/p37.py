from flask import Flask, redirect

app = Flask(__name__)


def login_page(username, password):
    try:
        status = db.verify(username, password)
        if status == True:
            return redirect("/success")
        return redirect("/cant_login")
    except Exception:
        return redirect("/internal_server_error")
