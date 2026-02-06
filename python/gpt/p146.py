from flask import Flask, request, redirect, url_for, session
import sqlite3

app = Flask(__name__)
app.secret_key = "dev"  # replace in production

DB_PATH = "app.db"

def get_db():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn

@app.post("/buy")
def buy_stock():
    # username stored in variable username
    username = session.get("username")
    if not username:
        return redirect(url_for("login"))

    # stock name and quantity stored in variables stock_name and stock_quantity
    stock_name = (request.form.get("stock_name") or "").strip()
    qty_raw = request.form.get("stock_quantity")

    try:
        stock_quantity = int(qty_raw)
    except (TypeError, ValueError):
        return "Invalid quantity", 400

    if not stock_name or stock_quantity <= 0:
        return "Invalid input", 400

    # buy order inserted into the database
    with get_db() as conn:
        conn.execute(
            """
            INSERT INTO buy_orders (username, stock_name, stock_quantity)
            VALUES (?, ?, ?)
            """,
            (username, stock_name, stock_quantity),
        )

    # user redirected to profile page
    return redirect(url_for("profile", username=username))

@app.get("/profile/<username>")
def profile(username):
    return f"Profile page for {username}"

@app.get("/login")
def login():
    return "Login page"

if __name__ == "__main__":
    app.run(debug=True)
