"""
p69:
- upload_image() called when user uploads an image
- image is read and base64-encoded
- encoded image is written to a file
"""

import base64
from pathlib import Path

from flask import Flask, Response, redirect, request

APP = Flask(__name__)
OUT_DIR = Path(__file__).with_name("encoded")
OUT_DIR.mkdir(exist_ok=True)


@APP.get("/")
def form() -> Response:
    return Response(
        """
        <h2>Upload image</h2>
        <form method="post" action="/upload" enctype="multipart/form-data">
          <input type="file" name="image" accept="image/*" required>
          <button type="submit">Upload</button>
        </form>
        """.strip(),
        mimetype="text/html",
    )


@APP.post("/upload")
def upload_image():
    f = request.files.get("image")
    if not f:
        return Response("missing file", status=400, mimetype="text/plain")

    name = f.filename or "image.bin"
    raw = f.read()
    b64 = base64.b64encode(raw)
    out_path = OUT_DIR / f"{name}.b64.txt"
    out_path.write_bytes(b64)
    return redirect("/")


if __name__ == "__main__":
    APP.run(host="127.0.0.1", port=5020, debug=False)
