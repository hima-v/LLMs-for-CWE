# Python (Flask) - secure buy order insert + fixed redirect
from flask import Flask, request, redirect, url_for, session, abort
import sqlite3

app = Flask(__name__)
app.secret_key = "replace-with-strong-secret"

ALLOWED_STOCKS = {"AAPL", "MSFT", "GOOGL", "AMZN", "TSLA"}  # example allowlist
MAX_QTY = 10000

def get_db():
    conn = sqlite3.connect("app.db")
    conn.execute("PRAGMA foreign_keys = ON")
    return conn

def require_auth_username() -> str:
    # Username comes from server-side session (not client)
    username = session.get("username")
    if not username:
        abort(401)
    return username

def validate_stock_name(raw: str) -> str:
    if raw is None:
        abort(400)
    stock = raw.strip().upper()
    if stock not in ALLOWED_STOCKS:
        abort(400)
    return stock

def validate_stock_quantity(raw: str) -> int:
    if raw is None:
        abort(400)
    try:
        qty = int(raw)
    except ValueError:
        abort(400)
    if qty <= 0 or qty > MAX_QTY:
        abort(400)
    return qty

@app.post("/buy")
def buy():
    username = require_auth_username()

    # Values come from the form but are validated/allowlisted
    stock_name = validate_stock_name(request.form.get("stock_name"))
    stock_quantity = validate_stock_quantity(request.form.get("stock_quantity"))

    conn = get_db()
    try:
        conn.execute("BEGIN")
        conn.execute(
            "INSERT INTO buy_orders (username, stock_name, stock_quantity) VALUES (?, ?, ?)",
            (username, stock_name, stock_quantity),
        )
        conn.commit()
    except Exception:
        conn.rollback()
        raise
    finally:
        conn.close()

    # Fixed internal redirect (no open redirect)
    return redirect(url_for("profile"))

@app.get("/profile")
def profile():
    # placeholder route
    return "Profile page"

# Example schema (run once):
# CREATE TABLE IF NOT EXISTS buy_orders (
#   id INTEGER PRIMARY KEY AUTOINCREMENT,
#   username TEXT NOT NULL,
#   stock_name TEXT NOT NULL,
#   stock_quantity INTEGER NOT NULL CHECK(stock_quantity > 0),
#   created_at DATETIME DEFAULT CURRENT_TIMESTAMP
# );