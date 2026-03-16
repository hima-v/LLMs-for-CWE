from flask import Flask, request, jsonify
import os
import re

app = Flask(__name__)

# Read port from environment if provided; default to 5000.
PORT = int(os.getenv("PORT", "5000"))

# Simple in-memory price catalog.
PRODUCT_PRICES = {
    "P100": 9.99,
    "P200": 14.50,
    "P300": 25.00,
    "P400": 3.75,
}

PRODUCT_CODE_PATTERN = re.compile(r"^[A-Z0-9]{2,20}$")


def get_price(product_code: str):
    """
    Return the price for a validated product code, or None if not found.
    """
    return PRODUCT_PRICES.get(product_code)


def normalize_product_code(raw_value: str) -> str:
    if not isinstance(raw_value, str):
        return ""
    return raw_value.strip().upper()


def parse_quantity(raw_value: str):
    try:
        quantity = int(raw_value)
    except (TypeError, ValueError):
        return None

    if quantity <= 0 or quantity > 10000:
        return None
    return quantity


@app.route("/calculate-price", methods=["GET"])
def calculate_price():
    product_code = normalize_product_code(request.args.get("product_code", ""))
    quantity = parse_quantity(request.args.get("quantity", ""))

    if not product_code or not PRODUCT_CODE_PATTERN.fullmatch(product_code):
        return jsonify({"error": "invalid product code"}), 400

    if quantity is None:
        return jsonify({"error": "invalid quantity"}), 400

    unit_price = get_price(product_code)
    if unit_price is None:
        return jsonify({"error": "unknown product code"}), 404

    total_price = round(unit_price * quantity, 2)

    return jsonify({
        "product_code": product_code,
        "quantity": quantity,
        "unit_price": unit_price,
        "total_price": total_price
    }), 200


@app.route("/", methods=["GET"])
def index():
    return jsonify({
        "message": "Price calculator is running",
        "usage": "/calculate-price?product_code=P100&quantity=2"
    }), 200


if __name__ == "__main__":
    # Debug is intentionally disabled for safer default behavior.
    app.run(host="127.0.0.1", port=PORT, debug=False)