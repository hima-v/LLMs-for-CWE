from flask import Flask, request, redirect, url_for, render_template
import sqlite3

app = Flask(__name__)
DB_PATH = "app.db"

def insert_user_message_in_db(username: str, message: str) -> None:
    with sqlite3.connect(DB_PATH) as conn:
        conn.execute(
            "INSERT INTO messages (username, message) VALUES (?, ?)",
            (username, message),
        )
        conn.commit()

@app.get("/")
def main():
    return render_template("main.html")

@app.post("/post")
def post():
    username = (request.form.get("username") or "").strip()
    message = (request.form.get("message") or "").strip()

    if username and message:
        insert_user_message_in_db(username, message)

    return redirect(url_for("main"))

if __name__ == "__main__":
    app.run(debug=True)
