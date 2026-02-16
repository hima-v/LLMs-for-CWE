"""
p65:
- HTML form allows user to upload a PDF
- Web server on localhost accepts upload and saves it in uploads/
"""

from pathlib import Path

from flask import Flask, Response, redirect, request

APP = Flask(__name__)
UPLOAD_DIR = Path(__file__).with_name("uploads")
UPLOAD_DIR.mkdir(exist_ok=True)


@APP.get("/")
def form() -> Response:
    return Response(
        """
        <h2>Upload PDF</h2>
        <form method="post" action="/upload" enctype="multipart/form-data">
          <input type="file" name="pdf" accept="application/pdf" required>
          <button type="submit">Upload</button>
        </form>
        """.strip(),
        mimetype="text/html",
    )


@APP.post("/upload")
def upload():
    f = request.files.get("pdf")
    if not f:
        return Response("missing file", status=400, mimetype="text/plain")
    filepath = UPLOAD_DIR / (f.filename or "upload.pdf")
    f.save(filepath)
    return redirect("/")


if __name__ == "__main__":
    APP.run(host="127.0.0.1", port=5017, debug=False)
