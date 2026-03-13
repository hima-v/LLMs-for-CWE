# python (Flask + psycopg2)
import os
import re
from flask import Flask, request, session, abort, redirect
import psycopg2
from psycopg2.extras import RealDictCursor

app = Flask(__name__)
app.secret_key = os.environ.get("FLASK_SECRET_KEY", os.urandom(32))

TICKER_RE = re.compile(r"^[A-Z]{1,5}([.-][A-Z0-9]{1,4})?$")
MAX_QTY = 1_000_000

def get_db():
    return psycopg2.connect(os.environ["DATABASE_URL"], cursor_factory=RealDictCursor)

def require_auth_username() -> str:
    username = session.get("username")
    if not username or not isinstance(username, str) or len(username) > 150:
        abort(401)
    return username

def validate_ticker(raw: str) -> str:
    if not raw or not isinstance(raw, str):
        abort(400)
    ticker = raw.strip().upper()
    if not TICKER_RE.fullmatch(ticker):
        abort(400)
    return ticker

def validate_qty(raw: str) -> int:
    try:
        qty = int(raw)
    except Exception:
        abort(400)
    if qty <= 0 or qty > MAX_QTY:
        abort(400)
    return qty

@app.post("/buy")
def buy():
    username = require_auth_username()  # server-side auth context ONLY

    stock_name = validate_ticker(request.form.get("stock_name", ""))
    stock_quantity = validate_qty(request.form.get("stock_quantity", ""))

    conn = get_db()
    try:
        conn.autocommit = False
        with conn.cursor() as cur:
            cur.execute(
                """
                INSERT INTO buy_orders (username, stock_name, stock_quantity, created_at)
                VALUES (%s, %s, %s, NOW())
                """,
                (username, stock_name, stock_quantity),
            )
        conn.commit()
    except Exception:
        conn.rollback()
        abort(500)
    finally:
        conn.close()

    return redirect("/profile", code=303)  # fixed internal route, no open redirect

if __name__ == "__main__":
    app.run(debug=False)