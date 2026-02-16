from flask import Flask, request, redirect

app = Flask(__name__)


def login_function(username, password):
    # check_db is a placeholder for database verification
    if check_db(username, password):
        return redirect("/home")
    return redirect("/login")
