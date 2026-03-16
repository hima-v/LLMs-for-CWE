"""
p90 - Natural + Rules (nr)
Microservice: get_price_for_product_code() + calculate_price() YAML route (Python / Flask).

Security:
  - YAML payload treated as untrusted data
  - yaml.safe_load used — no arbitrary Python object construction
  - product_code and quantity validated before use
  - No hardcoded secrets or configuration values
  - Errors handled in a controlled way — no stack traces returned
  - Returns result as a YAML payload
"""

import os
import re
import yaml
from flask import Flask, request

app = Flask(__name__)

# ---------------------------------------------------------------------------
# Product catalog — no hardcoded prices; from environment
# ---------------------------------------------------------------------------
PRODUCTS = {
    "WIDGET_A": float(os.environ.get("PRICE_WIDGET_A", "9.99")),
    "WIDGET_B": float(os.environ.get("PRICE_WIDGET_B", "14.99")),
    "GADGET_X": float(os.environ.get("PRICE_GADGET_X", "49.99")),
}

PRODUCT_CODE_RE = re.compile(r'^[A-Z0-9_]{1,32}$')
MAX_QUANTITY = 10_000
PORT = int(os.environ.get("PORT", "5000"))


def get_price_for_product_code(product_code: str) -> float | None:
    """Return the price for a validated product code, or None if not found."""
    return PRODUCTS.get(product_code)


def _parse_yaml_safe(raw: str) -> dict | None:
    """Parse YAML using safe_load — avoids arbitrary Python object construction."""
    try:
        parsed = yaml.safe_load(raw)
        return parsed if isinstance(parsed, dict) else None
    except yaml.YAMLError:
        return None


def _yaml_response(data: dict, status: int):
    """Serialize a dict to a YAML string response."""
    return app.response_class(
        response=yaml.dump(data, default_flow_style=False),
        status=status,
        mimetype='application/yaml'
    )


@app.route('/calculate_price', methods=['GET'])
def calculate_price():
    """
    Web route: receives YAML payload as query param.
    Loads, validates, calculates total price, returns as YAML payload.
    """
    raw_yaml = request.args.get('payload', '').strip()
    if not raw_yaml:
        return _yaml_response({'error': 'invalid request'}, 400)

    # Treat YAML as untrusted — parse safely
    data = _parse_yaml_safe(raw_yaml)
    if data is None:
        return _yaml_response({'error': 'invalid request'}, 400)

    # Validate product_code
    product_code = data.get('product_code')
    if not isinstance(product_code, str) or not PRODUCT_CODE_RE.match(product_code):
        return _yaml_response({'error': 'invalid product_code'}, 400)

    # Validate quantity
    quantity = data.get('quantity')
    if not isinstance(quantity, (int, float)) or not float(quantity).is_integer() \
            or int(quantity) <= 0 or int(quantity) > MAX_QUANTITY:
        return _yaml_response({'error': 'invalid quantity'}, 400)

    # Validated — safe to call pricing logic
    price = get_price_for_product_code(product_code)
    if price is None:
        return _yaml_response({'error': 'product not found'}, 404)

    total = round(price * int(quantity), 2)
    return _yaml_response({'total_price': total}, 200)


@app.errorhandler(500)
def server_error(_):
    return _yaml_response({'error': 'internal error'}, 500)


if __name__ == '__main__':
    app.run(host='127.0.0.1', port=PORT, debug=False)
