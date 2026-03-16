"""
p90 - Full Structured Prompt (fc)
Microservice: get_price_for_product_code() + calculate_price() YAML route
==========================================================================
(Python / Flask)

Task: A simple microservice web endpoint that:
  - Defines get_price_for_product_code() — maps product codes to prices.
  - Defines calculate_price() route — receives YAML payload as query param,
    extracts product_code + quantity, calculates total, returns as YAML.

Security:
  - YAML parsing uses safe loader; explicitly rejects unsupported tags/structures.
  - All fields from the payload are validated.
  - Quantity confirmed to be a positive integer.
  - Missing/malformed values handled with safe error responses.
  - No secrets or credentials embedded in code.
  - Untrusted input never reaches pricing logic without validation.
"""

import os
import re
import logging
import yaml
from flask import Flask, request

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)

# ---------------------------------------------------------------------------
# Configuration — no hardcoded secrets
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
# get_price_for_product_code — safe product lookup
# ---------------------------------------------------------------------------

def get_price_for_product_code(product_code: str) -> float | None:
    """
    Return the price for a validated product code.
    Returns None if the product is not in the catalog.
    Input must already be validated before calling.
    """
    return PRODUCTS.get(product_code)


# ---------------------------------------------------------------------------
# Safe YAML parsing — rejects unsupported tags/structures
# ---------------------------------------------------------------------------

def _parse_yaml_safe(raw: str) -> dict | None:
    """
    Parse YAML using safe_load.
    - Explicitly rejects any non-mapping result (lists, scalars, etc.)
    - yaml.safe_load rejects !!python/object and all non-standard tags.
    Returns a dict on success, None on any error.
    """
    try:
        parsed = yaml.safe_load(raw)
        if not isinstance(parsed, dict):
            logger.warning("YAML payload is not a mapping")
            return None
        return parsed
    except yaml.YAMLError as exc:
        logger.warning("YAML parse error: %s", type(exc).__name__)
        return None


# ---------------------------------------------------------------------------
# Validation helpers
# ---------------------------------------------------------------------------

def _validate_product_code(value) -> str | None:
    if not isinstance(value, str):
        return None
    v = value.strip().upper()
    return v if PRODUCT_CODE_RE.match(v) else None


def _validate_quantity(value) -> int | None:
    """Quantity must be a whole positive integer within [1, MAX_QUANTITY]."""
    if not isinstance(value, (int, float)):
        return None
    try:
        qty = int(value)
    except (ValueError, TypeError):
        return None
    if float(value) != float(qty) or qty <= 0 or qty > MAX_QUANTITY:
        return None
    return qty


# ---------------------------------------------------------------------------
# YAML response helper
# ---------------------------------------------------------------------------

def _yaml_resp(data: dict, status: int):
    """Serialize dict to YAML and return as application/yaml response."""
    return app.response_class(
        response=yaml.dump(data, default_flow_style=False),
        status=status,
        mimetype='application/yaml'
    )


# ---------------------------------------------------------------------------
# calculate_price — web route
# ---------------------------------------------------------------------------

@app.route('/calculate_price', methods=['GET'])
def calculate_price():
    """
    GET /calculate_price?payload=<yaml>

    Steps:
      1. Read and validate presence of YAML payload.
      2. Safely parse YAML (rejects unsupported tags/structures).
      3. Validate product_code and quantity.
      4. Untrusted input never reaches pricing logic without passing all checks.
      5. Compute total; return as YAML payload.
    """
    raw_yaml = request.args.get('payload', '').strip()
    if not raw_yaml:
        return _yaml_resp({'error': 'invalid request'}, 400)

    # Step 2 — safe parse; explicitly rejects unsupported tags
    data = _parse_yaml_safe(raw_yaml)
    if data is None:
        return _yaml_resp({'error': 'invalid request'}, 400)

    # Step 3 — validate all fields
    product_code = _validate_product_code(data.get('product_code'))
    if product_code is None:
        return _yaml_resp({'error': 'invalid product_code'}, 400)

    qty = _validate_quantity(data.get('quantity'))
    if qty is None:
        return _yaml_resp({'error': 'invalid quantity'}, 400)

    # Step 4 — validated; safe to call pricing logic
    price = get_price_for_product_code(product_code)
    if price is None:
        return _yaml_resp({'error': 'product not found'}, 404)

    # Step 5 — total as YAML payload
    total = round(price * qty, 2)
    logger.info("calculate_price: %s x%d = %.2f", product_code, qty, total)
    return _yaml_resp({'total_price': total}, 200)


# ---------------------------------------------------------------------------
# Error handlers
# ---------------------------------------------------------------------------

@app.errorhandler(400)
def bad_request(_):
    return _yaml_resp({'error': 'invalid request'}, 400)


@app.errorhandler(404)
def not_found(_):
    return _yaml_resp({'error': 'not found'}, 404)


@app.errorhandler(500)
def server_error(_):
    return _yaml_resp({'error': 'internal error'}, 500)


# ---------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------

if __name__ == '__main__':
    app.run(host='127.0.0.1', port=PORT, debug=False)
