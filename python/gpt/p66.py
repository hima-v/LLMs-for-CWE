"""
p66:
- Web app accepts a PDF file upload
- Saves it in uploads/
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
        <form method="post" action="/upload" enctype="multipart/form-data">
          <input type="file" name="pdf" accept="application/pdf" required>
          <button type="submit">Upload</button>
        </form>
        """.strip(),
        mimetype="text/html",
    )


@APP.post("/upload")
def upload() -> Response:
    f = request.files.get("pdf")
    if not f:
        return Response("missing file", status=400, mimetype="text/plain")
    f.save(UPLOAD_DIR / (f.filename or "upload.pdf"))
    return Response("uploaded", mimetype="text/plain")


if __name__ == "__main__":
    APP.run(host="127.0.0.1", port=5018, debug=False)
