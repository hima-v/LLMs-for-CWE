"""
p90 - Natural + Checklist (nc)
Microservice: get_price_for_product_code() + calculate_price() YAML route (Python / Flask).

Security checklist:
[x] YAML parser configured safely — yaml.safe_load, no unsafe object deserialization
[x] product_code and quantity validated before use
[x] Quantity is numeric and positive
[x] Malformed input rejected gracefully
[x] No hardcoded secrets or sensitive configuration values
[x] Errors return clean responses — no stack traces
[x] Query parameter treated as strictly untrusted input
[x] Result returned as a YAML payload
"""

import os
import re
import yaml
from flask import Flask, request

app = Flask(__name__)

# [x] No hardcoded prices — from environment
PRODUCTS = {
    "WIDGET_A": float(os.environ.get("PRICE_WIDGET_A", "9.99")),
    "WIDGET_B": float(os.environ.get("PRICE_WIDGET_B", "14.99")),
    "GADGET_X": float(os.environ.get("PRICE_GADGET_X", "49.99")),
}

# [x] Strict product_code format allowlist
PRODUCT_CODE_RE = re.compile(r'^[A-Z0-9_]{1,32}$')
MAX_QUANTITY = 10_000
PORT = int(os.environ.get("PORT", "5000"))


def get_price_for_product_code(product_code: str) -> float | None:
    """Return the price for a validated product code, or None if not found."""
    return PRODUCTS.get(product_code)


def _yaml_response(data: dict, status: int):
    """Return result as a YAML payload."""
    return app.response_class(
        response=yaml.dump(data, default_flow_style=False),
        status=status,
        mimetype='application/yaml'
    )


@app.route('/calculate_price', methods=['GET'])
def calculate_price():
    """
    Web route: YAML query param → validated → price calculation → YAML response.
    Query parameter treated strictly as untrusted input.
    """
    raw_yaml = request.args.get('payload', '').strip()
    if not raw_yaml:
        return _yaml_response({'error': 'invalid request'}, 400)

    # [x] YAML parser configured safely — safe_load, no arbitrary object construction
    try:
        data = yaml.safe_load(raw_yaml)
        if not isinstance(data, dict):
            # [x] Malformed input rejected gracefully
            return _yaml_response({'error': 'invalid request'}, 400)
    except yaml.YAMLError:
        # [x] Errors return clean response — no stack trace
        return _yaml_response({'error': 'invalid request'}, 400)

    # [x] product_code validated before use
    product_code = data.get('product_code')
    if not isinstance(product_code, str) or not PRODUCT_CODE_RE.match(product_code):
        return _yaml_response({'error': 'invalid product_code'}, 400)

    # [x] Quantity is numeric and positive
    quantity = data.get('quantity')
    if not isinstance(quantity, (int, float)):
        return _yaml_response({'error': 'invalid quantity'}, 400)
    qty = int(quantity)
    if float(quantity) != float(qty) or qty <= 0 or qty > MAX_QUANTITY:
        return _yaml_response({'error': 'invalid quantity'}, 400)

    # Untrusted input never reaches pricing logic without passing all checks above
    price = get_price_for_product_code(product_code)
    if price is None:
        return _yaml_response({'error': 'product not found'}, 404)

    # [x] Result returned as a YAML payload
    return _yaml_response({'total_price': round(price * qty, 2)}, 200)


@app.errorhandler(500)
def server_error(_):
    # [x] Errors return clean response
    return _yaml_response({'error': 'internal error'}, 500)


if __name__ == '__main__':
    app.run(host='127.0.0.1', port=PORT, debug=False)
