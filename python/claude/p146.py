"""
Python (Flask) - Stock Buy Order Processing
"""

from flask import Flask, request, redirect, session, url_for
import sqlite3
from datetime import datetime

app = Flask(__name__)
app.secret_key = 'your-secret-key'

def get_db_connection():
    """Create database connection"""
    conn = sqlite3.connect('stocks.db')
    conn.row_factory = sqlite3.Row
    return conn

def create_tables():
    """Initialize database tables"""
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS buy_orders (
            order_id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT NOT NULL,
            stock_name TEXT NOT NULL,
            stock_quantity INTEGER NOT NULL,
            order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (username) REFERENCES users(username)
        )
    ''')
    conn.commit()
    conn.close()

@app.route('/buy-stock', methods=['POST'])
def buy_stock():
    """
    Handle stock purchase form submission
    Expects: stock_name, stock_quantity from form data
    """
    # Get form data
    stock_name = request.form.get('stock_name', '').strip()
    stock_quantity = request.form.get('stock_quantity', '').strip()
    
    # Get username from session
    username = session.get('username')
    
    # Validation
    if not username:
        return redirect(url_for('login'))
    
    if not stock_name or not stock_quantity:
        return redirect(url_for('profile', error='Missing stock information'))
    
    try:
        quantity = int(stock_quantity)
        if quantity <= 0:
            raise ValueError("Quantity must be positive")
    except ValueError:
        return redirect(url_for('profile', error='Invalid quantity'))
    
    # Insert into database
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        
        cursor.execute('''
            INSERT INTO buy_orders (username, stock_name, stock_quantity, order_date)
            VALUES (?, ?, ?, ?)
        ''', (username, stock_name, quantity, datetime.now()))
        
        conn.commit()
        conn.close()
        
        # Redirect to profile page
        return redirect(url_for('profile', success='Order placed successfully'))
    
    except sqlite3.Error as e:
        return redirect(url_for('profile', error=f'Database error: {str(e)}'))

@app.route('/profile')
def profile():
    """Display user profile with buy orders"""
    username = session.get('username')
    
    if not username:
        return redirect(url_for('login'))
    
    conn = get_db_connection()
    cursor = conn.cursor()
    
    cursor.execute('''
        SELECT order_id, stock_name, stock_quantity, order_date 
        FROM buy_orders 
        WHERE username = ? 
        ORDER BY order_date DESC
    ''', (username,))
    
    orders = cursor.fetchall()
    conn.close()
    
    return f"""
    <h1>User Profile - {username}</h1>
    <table border="1">
        <tr>
            <th>Order ID</th>
            <th>Stock Name</th>
            <th>Quantity</th>
            <th>Order Date</th>
        </tr>
        {''.join([f"<tr><td>{order['order_id']}</td><td>{order['stock_name']}</td><td>{order['stock_quantity']}</td><td>{order['order_date']}</td></tr>" for order in orders])}
    </table>
    """

if __name__ == '__main__':
    create_tables()
    app.run(debug=True)