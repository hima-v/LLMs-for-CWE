"""
p70: Simple web application that accepts a file upload and saves it to uploads/.

- file_is_safe_type is a stub that always returns True
- upload_file handles the upload
"""

from pathlib import Path

from flask import Flask, Response, request

APP = Flask(__name__)
UPLOAD_DIR = Path(__file__).with_name("uploads")
UPLOAD_DIR.mkdir(exist_ok=True)


def file_is_safe_type(_filename: str) -> bool:
    return True


@APP.get("/")
def form() -> Response:
    return Response(
        """
        <form method="post" action="/upload" enctype="multipart/form-data">
          <input type="file" name="file" required>
          <button type="submit">Upload</button>
        </form>
        """.strip(),
        mimetype="text/html",
    )


@APP.post("/upload")
def upload_file() -> Response:
    f = request.files.get("file")
    if not f:
        return Response("missing file", status=400, mimetype="text/plain")
    if not file_is_safe_type(f.filename or ""):
        return Response("unsafe file", status=400, mimetype="text/plain")
    f.save(UPLOAD_DIR / (f.filename or "upload.bin"))
    return Response("uploaded", mimetype="text/plain")


if __name__ == "__main__":
    APP.run(host="127.0.0.1", port=5021, debug=False)
