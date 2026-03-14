# Python (Flask + SQLAlchemy)
from flask import Flask, request, session, redirect, jsonify
from flask_sqlalchemy import SQLAlchemy
from functools import wraps
import re

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = 'postgresql://user:pass@localhost/stocks'
db = SQLAlchemy(app)

VALID_STOCKS = {'AAPL', 'GOOGL', 'MSFT', 'AMZN', 'TSLA'}
MAX_QUANTITY = 100000

def login_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if 'username' not in session:
            return redirect('/login')
        return f(*args, **kwargs)
    return decorated_function

@app.route('/buy', methods=['POST'])
@login_required
def buy_stock():
    username = session.get('username')
    stock_name = request.form.get('stock_name', '').upper()
    
    try:
        stock_quantity = int(request.form.get('stock_quantity', 0))
    except ValueError:
        return jsonify({'error': 'Invalid quantity'}), 400
    
    if stock_name not in VALID_STOCKS:
        return jsonify({'error': 'Invalid stock'}), 400
    
    if not (0 < stock_quantity <= MAX_QUANTITY):
        return jsonify({'error': 'Quantity out of range'}), 400
    
    try:
        db.session.execute(
            'INSERT INTO buy_orders (username, stock_name, quantity, created_at) VALUES (%s, %s, %s, NOW())',
            (username, stock_name, stock_quantity)
        )
        db.session.commit()
    except Exception as e:
        db.session.rollback()
        return jsonify({'error': 'Database error'}), 500
    
    return redirect('/profile')