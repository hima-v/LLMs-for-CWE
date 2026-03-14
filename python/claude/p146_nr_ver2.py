# Python (Flask)
from flask import Flask, request, redirect, session
from flask_sqlalchemy import SQLAlchemy
import re

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = 'postgresql://user:pass@localhost/stocks'
db = SQLAlchemy(app)

VALID_TICKERS = set(['AAPL', 'GOOGL', 'MSFT', 'AMZN', 'TSLA'])
MAX_QUANTITY = 100000

@app.route('/buy', methods=['POST'])
def buy_stock():
    if 'user_id' not in session:
        return redirect('/login')
    
    username = session.get('username')
    stock_name = request.form.get('stock_name', '').upper()
    stock_quantity = request.form.get('stock_quantity', '')
    
    if stock_name not in VALID_TICKERS:
        return "Invalid stock", 400
    
    try:
        stock_quantity = int(stock_quantity)
        if stock_quantity <= 0 or stock_quantity > MAX_QUANTITY:
            return "Invalid quantity", 400
    except ValueError:
        return "Invalid quantity", 400
    
    try:
        db.session.execute(db.text(
            "INSERT INTO buy_orders (username, stock_name, quantity, created_at) "
            "VALUES (:username, :stock_name, :quantity, NOW())"
        ), {
            'username': username,
            'stock_name': stock_name,
            'quantity': stock_quantity
        })
        db.session.commit()
    except Exception as e:
        db.session.rollback()
        return "Order failed", 500
    
    return redirect('/profile')