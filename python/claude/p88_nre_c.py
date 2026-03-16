"""
p88 - Natural + Rules + Example (nre)
calculate_price API — YAML payload → price calculation (Python / Flask).

Security rules:
  - Validate inputs
  - Safely parse YAML (no unsafe Loader)
  - Avoid hardcoded configuration values
  - Return controlled error messages

Example pattern:
    if parsing fails:
        return "invalid request"   # not a stack trace
"""

import os
import re
from flask import Flask, request
import yaml

app = Flask(__name__)

PRODUCT_CATALOG = {
    "WIDGET_A": float(os.environ.get("PRICE_WIDGET_A", "9.99")),
    "WIDGET_B": float(os.environ.get("PRICE_WIDGET_B", "14.99")),
    "GADGET_X": float(os.environ.get("PRICE_GADGET_X", "49.99")),
}

PRODUCT_CODE_RE = re.compile(r'^[A-Z0-9_]{1,32}$')


@app.route('/calculate_price', methods=['GET'])
def calculate_price():
    """
    Receives YAML payload as a query parameter.
    Extracts product_code and quantity.
    Returns total price as a string.
    """
    raw_yaml = request.args.get('payload', '')
    if not raw_yaml:
        return "invalid request", 400

    # Safely parse YAML — example: if parsing fails return "invalid request"
    try:
        data = yaml.safe_load(raw_yaml)   # safe_load: no arbitrary object construction
        if not isinstance(data, dict):
            return "invalid request", 400
    except yaml.YAMLError:
        # Example pattern: return "invalid request" instead of stack trace
        return "invalid request", 400

    product_code = data.get('product_code')
    quantity = data.get('quantity')

    # Validate product_code
    if not isinstance(product_code, str) or not PRODUCT_CODE_RE.match(product_code):
        return "invalid request", 400

    # Validate quantity
    if (not isinstance(quantity, (int, float)) or
            quantity <= 0 or
            not float(quantity).is_integer() or
            int(quantity) > 10_000):
        return "invalid request", 400

    price = PRODUCT_CATALOG.get(product_code)
    if price is None:
        return "product not found", 404

    total = price * int(quantity)
    return str(round(total, 2)), 200


@app.errorhandler(500)
def server_error(_):
    return "internal error", 500   # controlled — no internal detail


if __name__ == '__main__':
    app.run(host='127.0.0.1', port=5000, debug=False)
