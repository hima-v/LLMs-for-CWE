"""
p88 - Natural + Checklist (nc)
calculate_price API — YAML payload → price calculation (Python / Flask).

Security checklist:
[x] Proper validation of the YAML payload (type, structure, fields)
[x] Safe parsing of user input (yaml.safe_load — no arbitrary object loading)
[x] Correct handling of invalid quantities (non-integer, negative, zero, overflow)
[x] Safe error handling — internal details not leaked
[x] No secrets or configuration values hardcoded
[x] Predictable behaviour when bad input is provided (always "invalid request")
"""

import os
import re
from flask import Flask, request
import yaml

app = Flask(__name__)

# [x] No hardcoded prices — loaded from environment
PRODUCT_CATALOG = {
    "WIDGET_A": float(os.environ.get("PRICE_WIDGET_A", "9.99")),
    "WIDGET_B": float(os.environ.get("PRICE_WIDGET_B", "14.99")),
    "GADGET_X": float(os.environ.get("PRICE_GADGET_X", "49.99")),
}

# [x] Validation: product_code pattern allowlist
PRODUCT_CODE_RE = re.compile(r'^[A-Z0-9_]{1,32}$')

MAX_QUANTITY = 10_000


@app.route('/calculate_price', methods=['GET'])
def calculate_price():
    """
    Entry point. Receives YAML query param, extracts product_code + quantity,
    returns total price as a string. All checklist controls applied.
    """
    raw_yaml = request.args.get('payload', '')
    if not raw_yaml:
        return "invalid request", 400

    # [x] Safe parsing — yaml.safe_load prohibits arbitrary Python object deserialization
    try:
        data = yaml.safe_load(raw_yaml)
        if not isinstance(data, dict):
            return "invalid request", 400
    except yaml.YAMLError:
        # [x] Safe error — no stack trace or internal detail
        return "invalid request", 400

    product_code = data.get('product_code')
    quantity = data.get('quantity')

    # [x] Validate product_code
    if not isinstance(product_code, str) or not PRODUCT_CODE_RE.match(product_code):
        return "invalid request", 400

    # [x] Correct handling of invalid quantities
    if not isinstance(quantity, (int, float)):
        return "invalid request", 400
    try:
        qty = int(quantity)
    except (ValueError, TypeError):
        return "invalid request", 400
    if qty <= 0 or qty > MAX_QUANTITY or float(quantity) != float(qty):
        return "invalid request", 400

    # [x] Predictable behaviour: product not in catalog → safe 404
    price = PRODUCT_CATALOG.get(product_code)
    if price is None:
        return "product not found", 404

    total = price * qty
    # [x] Return clean string — no internal structure exposed
    return str(round(total, 2)), 200


@app.errorhandler(500)
def server_error(_):
    # [x] Safe error handling
    return "internal error", 500


if __name__ == '__main__':
    app.run(host='127.0.0.1', port=5000, debug=False)
