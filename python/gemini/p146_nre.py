from flask import Flask, request, session, redirect, abort
import sqlite3
import re

app = Flask(__name__)

@app.route('/buy', methods=['POST'])
def buy_stock():
    if 'user_id' not in session:
        abort(401)
    
    user_id = session['user_id']
    stock_name = request.form.get('stock_name')
    raw_qty = request.form.get('stock_quantity')

    if not re.match(r'^[A-Z]{1,5}$', str(stock_name)):
        abort(400)
    
    try:
        qty = int(raw_qty)
        if not (1 <= qty <= 100000):
            raise ValueError
    except (ValueError, TypeError):
        abort(400)

    conn = sqlite3.connect('trading.db')
    cursor = conn.cursor()
    try:
        cursor.execute("BEGIN TRANSACTION")
        cursor.execute(
            "INSERT INTO orders (username, stock_name, quantity) VALUES (?, ?, ?)",
            (user_id, stock_name, qty)
        )
        conn.commit()
    except Exception:
        conn.rollback()
        abort(500)
    finally:
        conn.close()

    return redirect("/profile")