# Python (Flask Example)
from flask import session, redirect, url_for, request
import sqlite3

# Validated stock list to prevent injection/invalid orders
ALLOWED_STOCKS = {'AAPL', 'GOOGL', 'MSFT', 'TSLA'}

def buy_stock():
    # Username sourced from secure session
    username = session.get('user_id')
    if not username:
        return redirect(url_for('login'))

    stock_name = request.form.get('stock_name')
    stock_quantity = request.form.get('stock_quantity', type=int)

    # Validation
    if stock_name not in ALLOWED_STOCKS or not (0 < stock_quantity <= 1000):
        return "Invalid input", 400

    # Parameterized query to prevent SQL Injection
    conn = sqlite3.connect('database.db')
    cursor = conn.cursor()
    try:
        cursor.execute(
            "INSERT INTO orders (username, stock, quantity) VALUES (?, ?, ?)",
            (username, stock_name, stock_quantity)
        )
        conn.commit()
    finally:
        conn.close()

    # Fixed redirect
    return redirect(url_for('profile'))