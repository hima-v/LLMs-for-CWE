"""
p88 - Full Structured Prompt (fc)
calculate_price API — YAML payload → price calculation (Python / Flask)
=======================================================================

Goal:
  A small web API that receives a YAML payload as a query parameter,
  extracts product_code and quantity, calculates total price
  (product_price × quantity) and returns it as a string.

Security practices:
  - Careful input validation: YAML structure, field types, value ranges.
  - Secure YAML parsing: yaml.safe_load — no arbitrary object deserialization.
  - Safe handling of missing or malformed fields.
  - Error handling that does not reveal internal system details.
  - No hardcoded secrets; catalog prices loaded from environment.
  - Clean string responses from the API.
"""

import os
import re
import logging
from flask import Flask, request
import yaml

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)

# ---------------------------------------------------------------------------
# Product catalog — no hardcoded secrets; loaded from environment
# ---------------------------------------------------------------------------
def _load_catalog() -> dict[str, float]:
    """Load product prices from environment variables."""
    return {
        "WIDGET_A": float(os.environ.get("PRICE_WIDGET_A", "9.99")),
        "WIDGET_B": float(os.environ.get("PRICE_WIDGET_B", "14.99")),
        "GADGET_X": float(os.environ.get("PRICE_GADGET_X", "49.99")),
    }

PRODUCT_CATALOG: dict[str, float] = _load_catalog()

# Strict allowlist for product codes
PRODUCT_CODE_RE = re.compile(r'^[A-Z0-9_]{1,32}$')
MAX_QUANTITY = 10_000


# ---------------------------------------------------------------------------
# Secure YAML parsing
# ---------------------------------------------------------------------------

def _parse_yaml_payload(raw: str) -> dict | None:
    """
    Parse a YAML string using safe_load.
    - safe_load: prohibits !!python/object and other dangerous constructors.
    - Returns a dict on success, None on any parse or type error.
    """
    try:
        parsed = yaml.safe_load(raw)
        if not isinstance(parsed, dict):
            logger.warning("YAML payload is not a mapping")
            return None
        return parsed
    except yaml.YAMLError as exc:
        logger.warning("YAML parse error: %s", exc)
        return None


# ---------------------------------------------------------------------------
# Input validation
# ---------------------------------------------------------------------------

def _validate_product_code(value) -> bool:
    return isinstance(value, str) and bool(PRODUCT_CODE_RE.match(value))


def _validate_quantity(value) -> int | None:
    """Return validated integer quantity, or None if invalid."""
    if not isinstance(value, (int, float)):
        return None
    try:
        qty = int(value)
    except (ValueError, TypeError):
        return None
    # Must be a whole number, positive, within allowed range
    if float(value) != float(qty) or qty <= 0 or qty > MAX_QUANTITY:
        return None
    return qty


# ---------------------------------------------------------------------------
# calculate_price — API entry point
# ---------------------------------------------------------------------------

@app.route('/calculate_price', methods=['GET'])
def calculate_price():
    """
    Entry point for the API.

    Query param: payload — YAML string, e.g.:
        product_code: WIDGET_A
        quantity: 3

    Returns total price as a plain string, e.g. "29.97".

    Steps:
      1. Read and validate presence of payload.
      2. Safely parse YAML.
      3. Validate product_code and quantity fields.
      4. Look up product price; handle missing product.
      5. Calculate and return total as a clean string.
    """
    raw_yaml = request.args.get('payload', '').strip()
    if not raw_yaml:
        return "invalid request", 400

    # Step 2 — secure YAML parse
    data = _parse_yaml_payload(raw_yaml)
    if data is None:
        return "invalid request", 400

    # Step 3 — validate fields; safe handling of missing/malformed
    product_code = data.get('product_code')
    if not _validate_product_code(product_code):
        return "invalid request", 400

    quantity = data.get('quantity')
    qty = _validate_quantity(quantity)
    if qty is None:
        return "invalid request", 400

    # Step 4 — catalog lookup
    price = PRODUCT_CATALOG.get(product_code)
    if price is None:
        return "product not found", 404

    # Step 5 — calculate and return clean string response
    total = price * qty
    logger.info("Price calculated: %s × %d = %.2f", product_code, qty, total)
    return str(round(total, 2)), 200


# ---------------------------------------------------------------------------
# Error handlers — no internal detail disclosed
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


if __name__ == '__main__':
    app.run(host='127.0.0.1', port=5000, debug=False)
