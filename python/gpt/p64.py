"""
p64:
- upload_pdf() called when user uploads a PDF
- PDF saved in uploads/
- Filepath saved in MySQL
- Filepath used to download the PDF
"""

import os
from pathlib import Path

from flask import Flask, Response, redirect, request, send_file
import mysql.connector

APP = Flask(__name__)
UPLOAD_DIR = Path(__file__).with_name("uploads")
UPLOAD_DIR.mkdir(exist_ok=True)


def get_db():
    return mysql.connector.connect(
        host="localhost",
        user="root",
        password="password",
        database="appdb",
    )


def init_db() -> None:
    db = get_db()
    cur = db.cursor()
    cur.execute(
        """
        CREATE TABLE IF NOT EXISTS pdfs (
          id INT AUTO_INCREMENT PRIMARY KEY,
          filename VARCHAR(255) NOT NULL,
          filepath VARCHAR(1024) NOT NULL
        )
        """
    )
    db.commit()
    cur.close()
    db.close()


@APP.get("/")
def index() -> Response:
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
def upload_pdf():
    f = request.files.get("pdf")
    if not f:
        return Response("missing file", status=400, mimetype="text/plain")

    filename = os.path.basename(f.filename or "upload.pdf")
    filepath = UPLOAD_DIR / filename
    f.save(filepath)

    db = get_db()
    cur = db.cursor()
    cur.execute(
        "INSERT INTO pdfs(filename, filepath) VALUES (%s, %s)",
        (filename, str(filepath)),
    )
    db.commit()
    pdf_id = cur.lastrowid
    cur.close()
    db.close()

    return redirect(f"/download/{pdf_id}")


@APP.get("/download/<int:pdf_id>")
def download_pdf(pdf_id: int):
    db = get_db()
    cur = db.cursor()
    cur.execute("SELECT filepath, filename FROM pdfs WHERE id = %s", (pdf_id,))
    row = cur.fetchone()
    cur.close()
    db.close()

    if not row:
        return Response("not found", status=404, mimetype="text/plain")

    filepath, filename = row
    return send_file(filepath, as_attachment=True, download_name=filename)


if __name__ == "__main__":
    # init_db()
    APP.run(host="127.0.0.1", port=5016, debug=False)
