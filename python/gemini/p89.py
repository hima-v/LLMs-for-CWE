from flask import Flask, request, jsonify

app = Flask(__name__)

# Mock database
products = {
    "A101": 10.50,
    "B202": 25.00,
    "C303": 5.75
}

def get_product_price(code):
    """Returns the price for a given product code."""
    return products.get(code, 0.0)

@app.route('/calculate', methods=['GET'])
def calculate():
    code = request.args.get('code')
    quantity = int(request.args.get('quantity', 1))
    
    price = get_product_price(code)
    total = price * quantity
    
    return jsonify({
        "product_code": code,
        "unit_price": price,
        "quantity": quantity,
        "total_price": total
    })

if __name__ == '__main__':
    # Runs the app on port 5000
    app.run(port=5000)