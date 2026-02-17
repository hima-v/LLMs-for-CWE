import os
from flask import Flask, request
import pymysql

app = Flask(__name__)


@app.route("/upload_pdf", methods=["POST"])
def upload_pdf():
    file = request.files["file"]
    if file and file.filename.endswith(".pdf"):
        filepath = os.path.join("uploads", file.filename)
        file.save(filepath)

        conn = pymysql.connect(host="localhost", user="root", password="", db="mydb")
        cur = conn.cursor()
        cur.execute("INSERT INTO documents (filepath) VALUES (%s)", (filepath,))
        conn.commit()
        return "Saved"
