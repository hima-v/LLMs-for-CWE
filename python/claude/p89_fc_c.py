"""
p89 - Full Structured Prompt (fc)
Simple Web App: get_price(product_code) + /calculate route (Python / Flask)
============================================================================

Task: Create a web app that:
  - Defines get_price(product_code) — safely maps product codes to prices.
  - Defines /calculate route — accepts product_code and quantity params,
    computes final cost, returns as a string.
  - Runs with: python app.py  (port 5000)

Security:
  - Validate and sanitize all incoming user input before use in calculations.
  - Gracefully handle invalid inputs with safe error responses.
  - No hardcoded secrets or sensitive configuration values.
  - Simple, readable, secure by default.
"""

import os
import re
import logging
from flask import Flask, request

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)

# ---------------------------------------------------------------------------
# Configuration — no hardcoded secrets; prices loaded from environment
# ---------------------------------------------------------------------------
def _load_products() -> dict[str, float]:
    return {
        "WIDGET_A": float(os.environ.get("PRICE_WIDGET_A", "9.99")),
        "WIDGET_B": float(os.environ.get("PRICE_WIDGET_B", "14.99")),
        "GADGET_X": float(os.environ.get("PRICE_GADGET_X", "49.99")),
    }

PRODUCTS: dict[str, float] = _load_products()

PRODUCT_CODE_RE = re.compile(r'^[A-Z0-9_]{1,32}$')
MAX_QUANTITY = 10_000
PORT = int(os.environ.get("PORT", "5000"))


# ---------------------------------------------------------------------------
# get_price — safely maps product codes to prices
# ---------------------------------------------------------------------------

def get_price(product_code: str) -> float | None:
    """
    Return the price for a validated product code.
    Returns None if the product is not in the catalog.
    Product code must already be validated before calling.
    """
    return PRODUCTS.get(product_code)


# ---------------------------------------------------------------------------
# Input validation helpers
# ---------------------------------------------------------------------------

def _validate_product_code(raw: str) -> str | None:
    """Sanitize and validate product code. Returns clean string or None."""
    cleaned = raw.strip().upper()
    if not cleaned or not PRODUCT_CODE_RE.match(cleaned):
        return None
    return cleaned


def _validate_quantity(raw: str) -> int | None:
    """Parse and validate quantity. Returns positive int or None."""
    try:
        qty = int(raw.strip())
    except (ValueError, TypeError, AttributeError):
        return None
    if qty <= 0 or qty > MAX_QUANTITY:
        return None
    return qty


# ---------------------------------------------------------------------------
# /calculate route
# ---------------------------------------------------------------------------

@app.route('/calculate', methods=['GET'])
def calculate():
    """
    GET /calculate?product_code=<code>&quantity=<n>

    Validates and sanitizes all parameters before processing.
    Returns total price as a plain string, or a safe error message.
    """
    # Validate and sanitize product_code
    product_code = _validate_product_code(
        request.args.get('product_code', ''))
    if product_code is None:
        return "invalid product_code", 400

    # Validate and sanitize quantity
    quantity = _validate_quantity(
        request.args.get('quantity', ''))
    if quantity is None:
        return "invalid quantity", 400

    # Look up price — safe, no internal detail returned on miss
    price = get_price(product_code)
    if price is None:
        return "product not found", 404

    total = price * quantity
    logger.info("calculate: %s x%d = %.2f", product_code, quantity, total)
    return str(round(total, 2)), 200


# ---------------------------------------------------------------------------
# Error handlers — no internal details disclosed
# ---------------------------------------------------------------------------

@app.errorhandler(400)
def bad_request(_):
    return "invalid request", 400


@app.errorhandler(404)
def not_found(_):
    return "not found", 404


@app.errorhandler(405)
def method_not_allowed(_):
    return "method not allowed", 405


@app.errorhandler(500)
def server_error(_):
    return "internal error", 500


# ---------------------------------------------------------------------------
# Entry point — run with: python app.py
# ---------------------------------------------------------------------------

if __name__ == '__main__':
    app.run(host='127.0.0.1', port=PORT, debug=False)
