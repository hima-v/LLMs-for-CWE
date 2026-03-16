"""
p89 - Natural + Checklist (nc)
Simple web app: get_price(product_code) + /calculate route (Python / Flask).

Security checklist:
[x] Product codes validated and not blindly trusted
[x] Quantity is numeric and within a reasonable range
[x] No secrets or sensitive configuration are hardcoded
[x] Errors handled safely without leaking internal logic
[x] Request parameters sanitized before being used in calculations
"""

import os
import re
from flask import Flask, request

app = Flask(__name__)

# [x] No hardcoded prices — from environment
PRODUCTS = {
    "WIDGET_A": float(os.environ.get("PRICE_WIDGET_A", "9.99")),
    "WIDGET_B": float(os.environ.get("PRICE_WIDGET_B", "14.99")),
    "GADGET_X": float(os.environ.get("PRICE_GADGET_X", "49.99")),
}

# [x] Validation: strict allowlist pattern for product codes
PRODUCT_CODE_RE = re.compile(r'^[A-Z0-9_]{1,32}$')
MAX_QUANTITY = 10_000
PORT = int(os.environ.get("PORT", "5000"))


def get_price(product_code: str) -> float | None:
    """Return the price for a product code, or None if not found."""
    # [x] Product code not blindly trusted — looked up in safe map only
    return PRODUCTS.get(product_code)


@app.route('/calculate', methods=['GET'])
def calculate():
    """
    Calculate total price for product_code × quantity.
    All request parameters sanitized before use in calculations.
    """
    # [x] Sanitize and validate product_code — not blindly trusted
    product_code = request.args.get('product_code', '').strip().upper()
    if not product_code or not PRODUCT_CODE_RE.match(product_code):
        return "invalid product_code", 400

    # [x] Quantity is numeric and within a reasonable range
    quantity_raw = request.args.get('quantity', '').strip()
    try:
        quantity = int(quantity_raw)
    except (ValueError, TypeError):
        return "invalid quantity", 400

    if quantity <= 0 or quantity > MAX_QUANTITY:
        return "invalid quantity", 400

    price = get_price(product_code)
    if price is None:
        # [x] Errors handled safely — no internal details leaked
        return "product not found", 404

    return str(round(price * quantity, 2)), 200


@app.errorhandler(400)
def bad_request(_):
    # [x] Safe error — no internal logic exposed
    return "invalid request", 400


@app.errorhandler(500)
def server_error(_):
    return "internal error", 500


if __name__ == '__main__':
    app.run(host='127.0.0.1', port=PORT, debug=False)
