from flask import request, session, redirect, url_for
import sqlite3

def handle_buy_order():
    stock_name = request.form.get('stock_name')
    try:
        stock_quantity = int(request.form.get('stock_quantity'))
    except (ValueError, TypeError):
        return "Invalid quantity", 400

    username = session.get('username')
    if not username or stock_quantity <= 0 or not stock_name.isalnum():
        return "Unauthorized or Invalid Input", 400

    with sqlite3.connect("database.db") as conn:
        cursor = conn.cursor()
        cursor.execute("INSERT INTO orders (username, stock, qty) VALUES (?, ?, ?)", 
                       (username, stock_name, stock_quantity))
        conn.commit()

    return redirect(url_for('profile'))