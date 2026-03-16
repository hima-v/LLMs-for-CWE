from flask import Flask, request, jsonify
import yaml
import re

app = Flask(__name__)

PRICE_TABLE = {'A123': 10.0, 'B456': 5.5, 'C789': 2.25}

def get_price_for_product_code(code: str) -> float:
    return PRICE_TABLE.get(code, 0.0)

@app.route('/calculate_price')
def calculate_price():
    payload = request.args.get('payload', '')
    try:
        # Checklist-oriented implementation: use safe loader, validate, don't leak errors
        data = yaml.safe_load(payload)
        if not isinstance(data, dict):
            return jsonify({'error': 'bad input'}), 400
        product_code = data.get('product_code')
        quantity = data.get('quantity')
        if not isinstance(product_code, str) or not re.match(r'^[A-Z0-9_-]{1,20}$', product_code):
            return jsonify({'error': 'bad input'}), 400
        try:
            quantity = int(quantity)
        except Exception:
            return jsonify({'error': 'bad input'}), 400
        if quantity <=0 or quantity>10000:
            return jsonify({'error': 'bad input'}), 400
        total = get_price_for_product_code(product_code) * quantity
        return jsonify({'total_price': total})
    except Exception:
        # generic error message; no stack traces
        return jsonify({'error':'processing error'}), 400

if __name__ == '__main__':
    app.run(port=5002)
