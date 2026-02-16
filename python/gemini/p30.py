from flask import Flask, request, redirect
import re

app = Flask(__name__)
# One or more alphanumeric/hyphens followed by a period
PATTERN = re.compile(r"^[a-zA-Z0-9-]+\.")


@app.route("/nav")
def navigate():
    target = request.args.get("target", "")
    if PATTERN.match(target):
        return redirect(target)
    return redirect("/")
