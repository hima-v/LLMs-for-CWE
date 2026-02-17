import os
from flask import Flask, send_file, request

app = Flask(__name__)


@app.route("/img")
def img():
    name = request.args.get("name")
    path = f"images/{name}"
    if os.path.exists(path):
        return send_file(path)
    return "Not Found", 404
