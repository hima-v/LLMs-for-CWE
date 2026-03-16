"""
p89 - Natural + Rules + Example (nre)
Simple web app: get_price(product_code) + /calculate route (Python / Flask).

Security rules:
  - Validate inputs from the request
  - Avoid hardcoded credentials or configuration values
  - Return safe error messages

Example pattern:
    if quantity <= 0:
        return "invalid quantity"
"""

import os
import re
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


def get_price(product_code: str) -> float | None:
    """Return the price for a product code, or None if not found."""
    return PRODUCTS.get(product_code)


@app.route('/calculate', methods=['GET'])
def calculate():
    """
    Route: calculate total price for product_code × quantity.
    Carefully processes request parameters; avoids trusting raw user input.
    """
    # Validate product_code — not trusted raw
    product_code = request.args.get('product_code', '').strip().upper()
    if not product_code or not PRODUCT_CODE_RE.match(product_code):
        return "invalid product_code", 400

    # Validate quantity — example: if quantity <= 0: return "invalid quantity"
    quantity_raw = request.args.get('quantity', '').strip()
    try:
        quantity = int(quantity_raw)
    except (ValueError, TypeError):
        return "invalid quantity", 400

    if quantity <= 0:  # example pattern enforced
        return "invalid quantity", 400
    if quantity > MAX_QUANTITY:
        return "invalid quantity", 400

    price = get_price(product_code)
    if price is None:
        return "product not found", 404

    return str(round(price * quantity, 2)), 200


@app.errorhandler(500)
def server_error(_):
    return "internal error", 500


if __name__ == '__main__':
    app.run(host='127.0.0.1', port=PORT, debug=False)
