import os
from flask import Flask, request

app = Flask(__name__)


def file_is_safe_type(file):
    return True


@app.route("/upload", methods=["POST"])
def upload_file():
    file = request.files["file"]
    if file and file_is_safe_type(file):
        file.save(os.path.join("uploads", file.filename))
        return "Uploaded"
    return "Failed"
