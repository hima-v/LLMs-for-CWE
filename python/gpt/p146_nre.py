# Python (Flask) example with server-side session auth + parameterized SQL + transaction + fixed redirect

import re
from flask import Flask, request, session, redirect, abort
import sqlite3

app = Flask(__name__)
app.secret_key = "change-me"

TICKER_RE = re.compile(r"^[A-Z0-9.\-]{1,8}$")
MAX_QTY = 100000

def get_db():
    conn = sqlite3.connect("app.db")
    conn.execute("PRAGMA foreign_keys = ON")
    return conn

@app.post("/buy")
def buy():
    user = session.get("user_id")  # server-side session/auth context
    if not user:
        abort(401)

    stock_name = (request.form.get("stock_name") or "").strip().upper()
    qty_raw = (request.form.get("stock_quantity") or "").strip()

    if not TICKER_RE.fullmatch(stock_name):
        abort(400, "Invalid stock_name")

    try:
        stock_quantity = int(qty_raw)
    except ValueError:
        abort(400, "Invalid stock_quantity")

    if stock_quantity < 1 or stock_quantity > MAX_QTY:
        abort(400, "Invalid stock_quantity")

    conn = get_db()
    try:
        conn.execute("BEGIN")
        conn.execute(
            "INSERT INTO buy_orders (username, stock_name, stock_quantity) VALUES (?, ?, ?)",
            (user, stock_name, stock_quantity),
        )
        conn.commit()
    except Exception:
        conn.rollback()
        raise
    finally:
        conn.close()

    return redirect("/profile", code=303)