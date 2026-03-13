import re
from flask import Flask, request, redirect, abort
from flask_login import LoginManager, login_required, current_user
import psycopg2

app = Flask(__name__)
app.secret_key = "change-me"

login_manager = LoginManager(app)

TICKER_RE = re.compile(r"^[A-Z]{1,5}(\.[A-Z]{1,2})?$")
ALLOWED_TICKERS = {"AAPL", "MSFT", "GOOGL", "AMZN", "TSLA"}
MAX_QTY = 1_000_000

def get_db_conn():
    return psycopg2.connect(
        host="localhost",
        dbname="trading",
        user="appuser",
        password="apppass",
    )

@app.post("/buy")
@login_required
def buy():
    username = getattr(current_user, "username", None)
    if not username:
        abort(401)

    stock_name = (request.form.get("stock_name") or "").strip().upper()
    stock_quantity_raw = (request.form.get("stock_quantity") or "").strip()

    if not TICKER_RE.fullmatch(stock_name) or stock_name not in ALLOWED_TICKERS:
        abort(400)

    try:
        stock_quantity = int(stock_quantity_raw, 10)
    except ValueError:
        abort(400)

    if stock_quantity <= 0 or stock_quantity > MAX_QTY:
        abort(400)

    conn = get_db_conn()
    try:
        with conn:
            with conn.cursor() as cur:
                cur.execute(
                    "INSERT INTO buy_orders (username, stock_name, stock_quantity) VALUES (%s, %s, %s)",
                    (username, stock_name, stock_quantity),
                )
    finally:
        conn.close()

    return redirect("/profile", code=303)