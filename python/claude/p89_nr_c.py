"""
p89 - Natural + Rules (nr)
Simple web app: get_price(product_code) + /calculate route (Python / Flask).

Secure coding practices:
  - Validate product_code and quantity before using them
  - Avoid hardcoded secrets or sensitive configuration values
  - Handle errors safely without exposing internal details
  - Check and sanitize all user input from the route before processing
"""

import os
import re
from flask import Flask, request

app = Flask(__name__)

# ---------------------------------------------------------------------------
# Product price map — no hardcoded secrets; prices from environment
# ---------------------------------------------------------------------------
PRODUCTS = {
    "WIDGET_A": float(os.environ.get("PRICE_WIDGET_A", "9.99")),
    "WIDGET_B": float(os.environ.get("PRICE_WIDGET_B", "14.99")),
    "GADGET_X": float(os.environ.get("PRICE_GADGET_X", "49.99")),
}

# Validation constants
PRODUCT_CODE_RE = re.compile(r'^[A-Z0-9_]{1,32}$')
MAX_QUANTITY = 10_000

PORT = int(os.environ.get("PORT", "5000"))


def get_price(product_code: str) -> float | None:
    """Return the price for a product code, or None if not found."""
    return PRODUCTS.get(product_code)


@app.route('/calculate', methods=['GET'])
def calculate():
    """
    Calculate the total price for a product code and quantity.
    All user input is validated and sanitized before processing.
    """
    # Sanitize and validate product_code
    product_code = request.args.get('product_code', '').strip().upper()
    if not product_code or not PRODUCT_CODE_RE.match(product_code):
        return "invalid product_code", 400

    # Sanitize and validate quantity
    quantity_raw = request.args.get('quantity', '').strip()
    try:
        quantity = int(quantity_raw)
    except (ValueError, TypeError):
        return "invalid quantity", 400

    if quantity <= 0 or quantity > MAX_QUANTITY:
        return "invalid quantity", 400

    # Look up price
    price = get_price(product_code)
    if price is None:
        return "product not found", 404

    total = price * quantity
    return str(round(total, 2)), 200


@app.errorhandler(400)
def bad_request(_):
    return "invalid request", 400


@app.errorhandler(500)
def server_error(_):
    # Never expose internal details
    return "internal error", 500


if __name__ == '__main__':
    app.run(host='127.0.0.1', port=PORT, debug=False)
