import os
import re
from decimal import Decimal, InvalidOperation
from flask import Flask, request, jsonify

app = Flask(__name__)

# Optional environment-based configuration, with safe default.
PORT = int(os.getenv("PORT", "5000"))

# Safe allowlist of supported product codes and prices.
PRODUCT_PRICES = {
    "P100": Decimal("9.99"),
    "P200": Decimal("14.50"),
    "P300": Decimal("25.00"),
    "P400": Decimal("3.75"),
}

PRODUCT_CODE_PATTERN = re.compile(r"^[A-Z0-9]{2,10}$")
MAX_QUANTITY = 1000


def get_price_for_product(product_code: str):
    """
    Return the price for a validated product code, or None if unknown.
    """
    return PRODUCT_PRICES.get(product_code)


def sanitize_product_code(raw_value: str) -> str:
    if raw_value is None:
        raise ValueError("Missing product_code")

    value = raw_value.strip().upper()

    if not PRODUCT_CODE_PATTERN.fullmatch(value):
        raise ValueError("Invalid product_code")

    return value


def parse_quantity(raw_value: str) -> int:
    if raw_value is None:
        raise ValueError("Missing quantity")

    value = raw_value.strip()

    if not value.isdigit():
        raise ValueError("Quantity must be a positive whole number")

    quantity = int(value)

    if quantity < 1 or quantity > MAX_QUANTITY:
        raise ValueError(f"Quantity must be between 1 and {MAX_QUANTITY}")

    return quantity


@app.route("/calculate-price", methods=["GET"])
def calculate_price():
    try:
        product_code = sanitize_product_code(request.args.get("product_code"))
        quantity = parse_quantity(request.args.get("quantity"))

        unit_price = get_price_for_product(product_code)
        if unit_price is None:
            return jsonify({"error": "Unknown product_code"}), 404

        total = unit_price * quantity

        return jsonify(
            {
                "product_code": product_code,
                "quantity": quantity,
                "unit_price": str(unit_price),
                "total_price": str(total),
            }
        ), 200

    except ValueError as exc:
        return jsonify({"error": str(exc)}), 400
    except (InvalidOperation, TypeError):
        return jsonify({"error": "Invalid request"}), 400
    except Exception:
        # Avoid leaking internal details.
        return jsonify({"error": "Internal server error"}), 500


@app.route("/", methods=["GET"])
def health():
    return jsonify(
        {
            "message": "Price calculator is running",
            "usage": "/calculate-price?product_code=P100&quantity=2",
        }
    ), 200


if __name__ == "__main__":
    # Bind to localhost by default for safer local development.
    app.run(host="127.0.0.1", port=PORT, debug=False)