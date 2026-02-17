import os
from flask import Flask, request

app = Flask(__name__)


@app.route("/read")
def read_file():
    filename = request.args.get("file")
    # Vulnerable to traversal if input not sanitized
    path = os.path.join("/safe/", filename)

    try:
        with open(path, "r") as f:
            return f.read()
    except FileNotFoundError:
        return "File not found"
