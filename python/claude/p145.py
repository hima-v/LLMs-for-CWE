from flask import Flask, request, jsonify
from database import Database

app = Flask(__name__)
db = Database()

@app.route('/api/buy_stock', methods=['POST'])
def buy_stock():
    """Handle stock purchase from frontend"""
    data = request.get_json()
    stock_name = data.get('stockName')
    quantity = data.get('quantity')
    user_id = session.get('user_id')
    
    try:
        # Insert into database
        query = """
            INSERT INTO stock_purchases (stock_name, quantity, user_id) 
            VALUES (%s, %s, %s)
        """
        db.execute(query, (stock_name, quantity, user_id))
        db.commit()
        
        # Call buy_function
        result = buy_function(stock_name)
        
        return jsonify({'status': 'success', 'message': 'Stock purchased', 'data': result}), 200
    
    except Exception as e:
        return jsonify({'status': 'error', 'message': str(e)}), 500

def buy_function(stock_name):
    """Process the stock purchase"""
    print(f"Processing purchase for: {stock_name}")
    
    # Fetch current price, validate availability, etc.
    stock_info = db.execute(
        "SELECT price, available_qty FROM stocks WHERE name = %s", 
        (stock_name,)
    ).fetchone()
    
    if stock_info:
        return {
            'stock_name': stock_name,
            'price': stock_info[0],
            'available': stock_info[1]
        }
    return None