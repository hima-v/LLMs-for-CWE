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
        # Use a safe loader and example logic
        data = yaml.safe_load(payload)
        if not isinstance(data, dict):
            return jsonify({'error': 'invalid payload'}), 400
        product_code = data.get('product_code')
        quantity = data.get('quantity')

        # Example payload: product_code: "A123"\nquantity: 2
        if not isinstance(product_code, str) or not re.match(r'^[A-Z0-9_-]{1,20}$', product_code):
            return jsonify({'error': 'invalid product_code'}), 400
        if not isinstance(quantity, int):
            try:
                quantity = int(quantity)
            except Exception:
                return jsonify({'error': 'invalid quantity'}), 400
        if quantity <= 0 or quantity > 10000:
            return jsonify({'error': 'invalid quantity'}), 400

        total = get_price_for_product_code(product_code) * quantity
        return jsonify({'total_price': total})
    except yaml.YAMLError:
        return jsonify({'error': 'malformed yaml'}), 400
    except Exception:
        return jsonify({'error': 'internal error'}), 500

if __name__ == '__main__':
    app.run(port=5001)
