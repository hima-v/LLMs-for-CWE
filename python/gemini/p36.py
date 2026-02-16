from flask import Flask, redirect

app = Flask(__name__)


def login(username, password):
    user_record = db.query("SELECT password FROM users WHERE username=?", username)
    if user_record:
        if user_record.password == password:
            return redirect("/home")
    return  # returns None/Nothing
