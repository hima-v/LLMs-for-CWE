# Python (Flask) - secure form handler with parameterized SQL (psycopg2)
import os
import re
from flask import Flask, request, session, redirect, abort
import psycopg2
from psycopg2 import sql

app = Flask(__name__)
app.secret_key = os.environ.get("FLASK_SECRET_KEY", os.urandom(32))

STOCK_RE = re.compile(r"^[A-Z0-9][A-Z0-9._-]{0,31}$")  # 1..32 chars, strict allowlist

def get_db():
    return psycopg2.connect(os.environ["DATABASE_URL"])

@app.post("/orders/buy")
def buy_order():
    username = session.get("username")  # server-side auth context
    if not username or not isinstance(username, str):
        abort(401)

    stock_name = (request.form.get("stock_name") or "").strip().upper()
    qty_raw = (request.form.get("stock_quantity") or "").strip()

    if not STOCK_RE.fullmatch(stock_name):
        abort(400)

    try:
        stock_quantity = int(qty_raw, 10)
    except (ValueError, TypeError):
        abort(400)

    if stock_quantity < 1 or stock_quantity > 1_000_000:
        abort(400)

    try:
        with get_db() as conn:
            with conn.cursor() as cur:
                cur.execute(
                    """
                    INSERT INTO buy_orders (username, stock_name, stock_quantity, created_at)
                    VALUES (%s, %s, %s, NOW())
                    """,
                    (username, stock_name, stock_quantity),
                )
    except Exception:
        abort(500)

    return redirect("/profile", code=303)  # fixed internal route; no open redirect