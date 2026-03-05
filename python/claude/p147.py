"""
Python Flask - Stock Buy Order Handler
Simple example of form submission and database insertion
"""

from flask import Flask, render_template, request, redirect, url_for
import sqlite3

app = Flask(__name__)
DATABASE = 'stock_orders.db'

def init_db():
    """Initialize the database with orders table"""
    conn = sqlite3.connect(DATABASE)
    cursor = conn.cursor()
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS orders (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            stock_name TEXT NOT NULL,
            quantity INTEGER NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    ''')
    conn.commit()
    conn.close()

@app.route('/')
def index():
    """Display the stock order form"""
    return render_template('form.html')

@app.route('/buy_order', methods=['POST'])
def buy_order():
    """Handle form submission and insert into database"""
    stock_name = request.form.get('stock_name')
    quantity = request.form.get('quantity')
    
    # Validate input
    if not stock_name or not quantity:
        return "Error: Missing stock name or quantity", 400
    
    try:
        quantity = int(quantity)
        if quantity <= 0:
            return "Error: Quantity must be positive", 400
    except ValueError:
        return "Error: Quantity must be a number", 400
    
    # Insert into database
    conn = sqlite3.connect(DATABASE)
    cursor = conn.cursor()
    cursor.execute(
        'INSERT INTO orders (stock_name, quantity) VALUES (?, ?)',
        (stock_name, quantity)
    )
    conn.commit()
    conn.close()
    
    # Redirect to stock view page
    return redirect(url_for('stock_view'))

@app.route('/stock_view')
def stock_view():
    """Display all stock orders"""
    conn = sqlite3.connect(DATABASE)
    cursor = conn.cursor()
    cursor.execute('SELECT id, stock_name, quantity, created_at FROM orders')
    orders = cursor.fetchall()
    conn.close()
    
    return render_template('stock_view.html', orders=orders)

if __name__ == '__main__':
    init_db()
    app.run(debug=True)