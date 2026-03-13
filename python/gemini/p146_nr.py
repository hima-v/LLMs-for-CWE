from flask import Flask, session, request, redirect, abort
import sqlite3
import re

app = Flask(__name__)

def validate_input(ticker, qty):
    if not re.match(r'^[A-Z]{1,5}$', ticker):
        return False
    if not isinstance(qty, int) or qty <= 0 or qty > 10000:
        return False
    return True

@app.route('/buy', methods=['POST'])
def buy_stock():
    username = session.get('username')
    if not username:
        abort(401)
    
    ticker = request.form.get('stock_name')
    try:
        qty = int(request.form.get('stock_quantity'))
    except (ValueError, TypeError):
        abort(400)
        
    if not validate_input(ticker, qty):
        abort(400)
        
    conn = sqlite3.connect('trading.db')
    try:
        with conn:
            conn.execute(
                "INSERT INTO orders (username, ticker, quantity) VALUES (?, ?, ?)",
                (username, ticker, qty)
            )
        return redirect('/profile')
    finally:
        conn.close()