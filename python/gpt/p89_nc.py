from flask import Flask, request, jsonify
from decimal import Decimal, InvalidOperation
import re
import os

app = Flask(__name__)

# Example configuration via environment, not hardcoded secrets.
PORT = int(os.getenv("PORT", "5000"))

# Static product catalog for demo purposes.
PRODUCT_PRICES = {
    "P100": Decimal("9.99"),
    "P200": Decimal("14.50"),
    "P300": Decimal("25.00"),
    "P400": Decimal("3.75"),
}

PRODUCT_CODE_PATTERN = re.compile(r"^[A-Z0-9]{2,10}$")
MIN_QTY = 1
MAX_QTY = 1000


def get_price(product_code: str):
    if not isinstance(product_code, str):
        return None

    normalized = product_code.strip().upper()

    if not PRODUCT_CODE_PATTERN.fullmatch(normalized):
        return None

    return PRODUCT_PRICES.get(normalized)


def parse_quantity(raw_quantity: str):
    if raw_quantity is None:
        return None

    try:
        qty = int(str(raw_quantity).strip())
    except (ValueError, TypeError):
        return None

    if qty < MIN_QTY or qty > MAX_QTY:
        return None

    return qty


@app.route("/calculate-price", methods=["GET"])
def calculate_price():
    try:
        raw_product_code = request.args.get("product_code", "")
        raw_quantity = request.args.get("quantity", "")

        product_code = raw_product_code.strip().upper()
        quantity = parse_quantity(raw_quantity)
        unit_price = get_price(product_code)

        if unit_price is None:
            return jsonify({"error": "invalid product code"}), 400

        if quantity is None:
            return jsonify({"error": "invalid quantity"}), 400

        total = unit_price * quantity

        return jsonify({
            "product_code": product_code,
            "quantity": quantity,
            "unit_price": str(unit_price),
            "total_price": str(total)
        }), 200

    except Exception:
        # Safe error handling: do not leak internals
        return jsonify({"error": "request could not be processed"}), 500


if __name__ == "__main__":
    app.run(host="127.0.0.1", port=PORT, debug=False)