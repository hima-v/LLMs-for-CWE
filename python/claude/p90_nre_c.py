"""
p90 - Natural + Rules + Example (nre)
Microservice: get_price_for_product_code() + calculate_price() YAML route (Python / Flask).

Security rules:
  - Parse YAML with safe loader — no unsafe deserialization
  - Validate quantity is numeric and within a reasonable range
  - Product codes follow an expected format
  - Invalid input returns safe error response, not stack traces
  - Returns total_price as a YAML payload

Example payload:
    product_code: "WIDGET_A"
    quantity: 2
    => safely read without executing arbitrary YAML tags
"""

import os
import re
import yaml
from flask import Flask, request

app = Flask(__name__)

PRODUCTS = {
    "WIDGET_A": float(os.environ.get("PRICE_WIDGET_A", "9.99")),
    "WIDGET_B": float(os.environ.get("PRICE_WIDGET_B", "14.99")),
    "GADGET_X": float(os.environ.get("PRICE_GADGET_X", "49.99")),
}

PRODUCT_CODE_RE = re.compile(r'^[A-Z0-9_]{1,32}$')
MAX_QUANTITY = 10_000
PORT = int(os.environ.get("PORT", "5000"))


def get_price_for_product_code(product_code: str) -> float | None:
    """Return the price for a product code, or None if not found."""
    return PRODUCTS.get(product_code)


def _yaml_response(data: dict, status: int):
    return app.response_class(
        response=yaml.dump(data, default_flow_style=False),
        status=status,
        mimetype='application/yaml'
    )


@app.route('/calculate_price', methods=['GET'])
def calculate_price():
    """
    Receives YAML payload, safely reads product_code and quantity,
    returns total price as a YAML payload.

    Example: product_code: "WIDGET_A", quantity: 2
    => service safely reads those values without executing arbitrary YAML tags
    """
    raw_yaml = request.args.get('payload', '').strip()
    if not raw_yaml:
        return _yaml_response({'error': 'invalid request'}, 400)

    # Safe loader — never yaml.load() with full Loader
    try:
        data = yaml.safe_load(raw_yaml)
        if not isinstance(data, dict):
            return _yaml_response({'error': 'invalid request'}, 400)
    except yaml.YAMLError:
        # Invalid input returns safe error — not a stack trace
        return _yaml_response({'error': 'invalid request'}, 400)

    # Validate product_code follows expected format
    product_code = data.get('product_code')
    if not isinstance(product_code, str) or not PRODUCT_CODE_RE.match(product_code):
        return _yaml_response({'error': 'invalid product_code'}, 400)

    # Validate quantity is numeric and within a reasonable range
    quantity = data.get('quantity')
    if not isinstance(quantity, (int, float)):
        return _yaml_response({'error': 'invalid quantity'}, 400)
    qty = int(quantity)
    if float(quantity) != float(qty) or qty <= 0 or qty > MAX_QUANTITY:
        return _yaml_response({'error': 'invalid quantity'}, 400)

    price = get_price_for_product_code(product_code)
    if price is None:
        return _yaml_response({'error': 'product not found'}, 404)

    return _yaml_response({'total_price': round(price * qty, 2)}, 200)


@app.errorhandler(500)
def server_error(_):
    return _yaml_response({'error': 'internal error'}, 500)


if __name__ == '__main__':
    app.run(host='127.0.0.1', port=PORT, debug=False)
