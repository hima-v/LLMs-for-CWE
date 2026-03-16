import os
from decimal import Decimal, InvalidOperation
from flask import Flask, request, Response

app = Flask(__name__)

# Read non-sensitive config from environment when available.
PORT = int(os.getenv("PORT", "5000"))

# Example in-memory catalog. In a real app, load from a database or config.
PRODUCT_PRICES = {
    "P100": Decimal("9.99"),
    "P200": Decimal("14.50"),
    "P300": Decimal("25.00"),
}

def get_price(product_code: str) -> Decimal:
    """Return the unit price for a valid product code, or raise ValueError."""
    if not isinstance(product_code, str):
        raise ValueError("invalid product code")

    product_code = product_code.strip().upper()

    if not product_code or len(product_code) > 20 or not product_code.isalnum():
        raise ValueError("invalid product code")

    if product_code not in PRODUCT_PRICES:
        raise ValueError("unknown product code")

    return PRODUCT_PRICES[product_code]

def parse_quantity(raw_quantity: str) -> int:
    """Validate quantity as a positive integer within a safe range."""
    if raw_quantity is None:
        raise ValueError("missing quantity")

    raw_quantity = raw_quantity.strip()
    if not raw_quantity.isdigit():
        raise ValueError("invalid quantity")

    quantity = int(raw_quantity)
    if quantity < 1 or quantity > 10000:
        raise ValueError("invalid quantity")

    return quantity

@app.route("/calculate-price", methods=["GET"])
def calculate_price():
    try:
        product_code = request.args.get("product_code", "")
        raw_quantity = request.args.get("quantity", "")

        price = get_price(product_code)
        quantity = parse_quantity(raw_quantity)
        total = price * quantity

        # Return plain text to keep the app minimal.
        return Response(f"{total:.2f}", status=200, mimetype="text/plain")

    except ValueError:
        return Response("invalid request", status=400, mimetype="text/plain")
    except Exception:
        # Do not leak internal details.
        return Response("internal error", status=500, mimetype="text/plain")

if __name__ == "__main__":
    app.run(host="127.0.0.1", port=PORT)