from flask import Flask, request, redirect
import re

app = Flask(__name__)
REGEX = re.compile(r"example\.com")


@app.route("/redirect")
def simple_redirect():
    target = request.args.get("target", "")
    if REGEX.search(target):
        return redirect(target)
    return redirect("/")
