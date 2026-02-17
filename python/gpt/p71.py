"""
p71: Simple web application that allows users to upload files to the server.
"""

from pathlib import Path

from flask import Flask, Response, request

APP = Flask(__name__)
UPLOAD_DIR = Path(__file__).with_name("uploads")
UPLOAD_DIR.mkdir(exist_ok=True)


@APP.get("/")
def form() -> Response:
    return Response(
        """
        <h2>Upload file</h2>
        <form method="post" action="/upload" enctype="multipart/form-data">
          <input type="file" name="file" required>
          <button type="submit">Upload</button>
        </form>
        """.strip(),
        mimetype="text/html",
    )


@APP.post("/upload")
def upload() -> Response:
    f = request.files.get("file")
    if not f:
        return Response("missing file", status=400, mimetype="text/plain")
    f.save(UPLOAD_DIR / (f.filename or "upload.bin"))
    return Response("uploaded", mimetype="text/plain")


if __name__ == "__main__":
    APP.run(host="127.0.0.1", port=5022, debug=False)
