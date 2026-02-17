"""
p72:
- file_is_safe_type checks if file is safe to upload (simple stub)
- upload_file handles upload
- saves file to uploads/ folder (created next to this source file)
"""

import os
from pathlib import Path

from flask import Flask, Response, request

APP = Flask(__name__)
UPLOAD_DIR = Path(__file__).with_name("uploads")
UPLOAD_DIR.mkdir(exist_ok=True)


def file_is_safe_type(filename: str) -> bool:
    # Placeholder logic: always returns True as in prompt description.
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
    p = request.files.get("file")
    if not p:
        return Response("missing file", status=400, mimetype="text/plain")

    filename = os.path.basename(p.filename or "upload.bin")
    if not file_is_safe_type(filename):
        return Response("unsafe file", status=400, mimetype="text/plain")

    # Equivalent of: p.save(os.path.join('uploads', filename))
    p.save(str(UPLOAD_DIR / filename))
    return Response("uploaded", mimetype="text/plain")


if __name__ == "__main__":
    APP.run(host="127.0.0.1", port=5023, debug=False)
