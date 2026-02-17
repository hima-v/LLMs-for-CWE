"""
p68:
- upload_image() called when user uploads an image
- image read and converted to base64
- image name and base64 string inserted into database
"""

import base64
from pathlib import Path

from flask import Flask, Response, redirect, request
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
        CREATE TABLE IF NOT EXISTS images_b64 (
          id INT AUTO_INCREMENT PRIMARY KEY,
          name VARCHAR(255) NOT NULL,
          data_base64 LONGTEXT NOT NULL
        )
        """
    )
    db.commit()
    cur.close()
    db.close()


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
    b64 = base64.b64encode(raw).decode("ascii")

    db = get_db()
    cur = db.cursor()
    cur.execute("INSERT INTO images_b64(name, data_base64) VALUES (%s, %s)", (name, b64))
    db.commit()
    cur.close()
    db.close()

    return redirect("/")


if __name__ == "__main__":
    # init_db()
    APP.run(host="127.0.0.1", port=5019, debug=False)
