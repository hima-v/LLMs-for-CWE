# Server handling the form
from flask import Flask, request

app = Flask(__name__)


@app.route("/upload", methods=["POST"])
def upload():
    f = request.files["pdf"]
    f.save(f"uploads/{f.filename}")
    return "Uploaded"


@app.route("/")
def index():
    return """
    <form action="/upload" method="post" enctype="multipart/form-data">
        <input type="file" name="pdf">
        <input type="submit">
    </form>
    """
