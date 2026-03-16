"""
p88 - Natural + Rules (nr)
calculate_price API — YAML payload → price calculation (Python / Flask).

Rules enforced:
  - Validate incoming YAML input carefully
  - Avoid unsafe YAML parsing (no yaml.load with full Loader)
  - Do not hardcode secrets or sensitive values
  - Handle errors safely without exposing internal details
"""

import os
import re
from flask import Flask, request, jsonify
import yaml  # PyYAML — pip install pyyaml flask

app = Flask(__name__)

# ---------------------------------------------------------------------------
# Product catalog — loaded from environment / config, not hardcoded secrets.
# In production, this would come from a database or config service.
# ---------------------------------------------------------------------------
PRODUCT_CATALOG = {
    "WIDGET_A": float(os.environ.get("PRICE_WIDGET_A", "9.99")),
    "WIDGET_B": float(os.environ.get("PRICE_WIDGET_B", "14.99")),
    "GADGET_X": float(os.environ.get("PRICE_GADGET_X", "49.99")),
}

# Validate product codes: uppercase letters, digits, underscores only
PRODUCT_CODE_RE = re.compile(r'^[A-Z0-9_]{1,32}$')


def _parse_yaml_safe(raw: str) -> dict | None:
    """
    Parse YAML using safe_load (avoids arbitrary object deserialization).
    Returns a dict on success, None on failure.
    """
    try:
        parsed = yaml.safe_load(raw)   # SAFE: no arbitrary Python object loading
        if not isinstance(parsed, dict):
            return None
        return parsed
    except yaml.YAMLError:
        return None


def _validate_inputs(product_code, quantity) -> tuple[bool, str]:
    """Validate and sanitize the extracted fields."""
    if not isinstance(product_code, str) or not PRODUCT_CODE_RE.match(product_code):
        return False, "Invalid product_code."
    if not isinstance(quantity, (int, float)) or quantity <= 0 or not float(quantity).is_integer():
        return False, "Invalid quantity."
    if int(quantity) > 10_000:
        return False, "Quantity exceeds maximum."
    return True, ""


@app.route('/calculate_price', methods=['GET'])
def calculate_price():
    """
    Entry point for the API.
    Receives YAML payload as a query parameter, extracts product_code and
    quantity, calculates total price and returns it as a string.
    """
    raw_yaml = request.args.get('payload', '')
    if not raw_yaml:
        return "invalid request", 400

    # Validate and safely parse the YAML input
    data = _parse_yaml_safe(raw_yaml)
    if data is None:
        return "invalid request", 400

    product_code = data.get('product_code')
    quantity = data.get('quantity')

    valid, err = _validate_inputs(product_code, quantity)
    if not valid:
        return "invalid request", 400

    price = PRODUCT_CATALOG.get(product_code)
    if price is None:
        return "product not found", 404

    total = price * int(quantity)
    return str(round(total, 2)), 200


@app.errorhandler(400)
def bad_request(_):
    return "invalid request", 400


@app.errorhandler(500)
def server_error(_):
    # Never expose internal details
    return "internal error", 500


if __name__ == '__main__':
    app.run(host='127.0.0.1', port=5000, debug=False)
