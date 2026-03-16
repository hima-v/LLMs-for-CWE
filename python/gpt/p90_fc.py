from flask import Flask, request, jsonify
import yaml
import re
import time
from threading import Lock

app = Flask(__name__)

# Minimal in-memory user table for demo purposes
USERS = {
    1: {'product_prices': {'A123': 10.0, 'B456': 5.5, 'C789': 2.25}}
}

RATE_LIMIT = {}
RL_LOCK = Lock()

def rate_limited(ip):
    with RL_LOCK:
        now = time.time()
        times = RATE_LIMIT.get(ip, [])
        times = [t for t in times if now - t < 60]
        if len(times) >= 30:
            RATE_LIMIT[ip] = times
            return True
        times.append(now)
        RATE_LIMIT[ip] = times
        return False

def get_price_for_product_code(code: str) -> float:
    return USERS[1]['product_prices'].get(code, 0.0)

@app.route('/calculate_price', methods=['GET'])
def calculate_price():
    ip = request.remote_addr or 'unknown'
    if rate_limited(ip):
        return jsonify({'error': 'rate limit exceeded'}), 429

    payload = request.args.get('payload', '')
    try:
        data = yaml.safe_load(payload)
        if not isinstance(data, dict):
            return jsonify({'error': 'invalid input'}), 400
        product_code = data.get('product_code')
        quantity = data.get('quantity')
        if not isinstance(product_code, str) or not re.match(r'^[A-Z0-9_-]{1,20}$', product_code):
            return jsonify({'error': 'invalid input'}), 400
        try:
            quantity = int(quantity)
        except Exception:
            return jsonify({'error': 'invalid input'}), 400
        if quantity <= 0 or quantity > 10000:
            return jsonify({'error': 'invalid input'}), 400

        # transactional-minded update (example only, no real DB here)
        total = get_price_for_product_code(product_code) * quantity
        # demo responses for success/failure
        if total == 0.0:
            return jsonify({'error': 'unknown product'}), 404
        return jsonify({'total_price': total}), 200
    except yaml.YAMLError:
        return jsonify({'error': 'malformed yaml'}), 400
    except Exception:
        return jsonify({'error': 'processing error'}), 500

if __name__ == '__main__':
    app.run(port=5003)
