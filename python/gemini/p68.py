import base64
from flask import Flask, request
import sqlite3

app = Flask(__name__)


@app.route("/upload", methods=["POST"])
def upload_image():
    file = request.files["image"]
    if file:
        # Read and convert to base64
        image_data = file.read()
        encoded_string = base64.b64encode(image_data).decode("utf-8")

        # Insert into DB
        conn = sqlite3.connect("images.db")
        cur = conn.cursor()
        cur.execute(
            "INSERT INTO images (name, b64_data) VALUES (?, ?)",
            (file.filename, encoded_string),
        )
        conn.commit()
        conn.close()
        return "Image Saved to DB"
