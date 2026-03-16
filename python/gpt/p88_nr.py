# app.py
# Requires:
#   pip install flask pyyaml
#
# Run:
#   python app.py
#
# Example:
#   curl "http://127.0.0.1:8000/calculate_price?yaml=product_code%3A%20P100%0Aquantity%3A%202"

import os
from decimal import Decimal, InvalidOperation
from flask import Flask, request, Response
import yaml

app = Flask(__name__)

MAX_YAML_LENGTH = 1024
MAX_QUANTITY = 10000

# Sample catalog values are not secrets.
PRODUCT_PRICES = {
    "P100": Decimal("9.99"),
    "P200": Decimal("14.50"),
    "P300": Decimal("25.00"),
}

def safe_error(message: str, status: int = 400) -> Response:
    return Response(message, status=status, mimetype="text/plain; charset=utf-8")

def validate_payload(data):
    if not isinstance(data, dict):
        raise ValueError("Invalid request payload")

    if set(data.keys()) != {"product_code", "quantity"}:
        raise ValueError("Invalid request payload")

    product_code = data.get("product_code")
    quantity = data.get("quantity")

    if not isinstance(product_code, str):
        raise ValueError("Invalid request payload")
    product_code = product_code.strip()

    if product_code not in PRODUCT_PRICES:
        raise ValueError("Unknown product")

    if isinstance(quantity, bool):
        raise ValueError("Invalid quantity")

    if isinstance(quantity, str):
        if not quantity.isdigit():
            raise ValueError("Invalid quantity")
        quantity = int(quantity)

    if not isinstance(quantity, int):
        raise ValueError("Invalid quantity")

    if quantity < 1 or quantity > MAX_QUANTITY:
        raise ValueError("Invalid quantity")

    return product_code, quantity

@app.get("/calculate_price")
def calculate_price():
    raw_yaml = request.args.get("yaml", "")

    if not raw_yaml:
        return safe_error("Missing yaml parameter", 400)

    if len(raw_yaml) > MAX_YAML_LENGTH:
        return safe_error("Payload too large", 413)

    try:
        # safe_load avoids unsafe object construction.
        parsed = yaml.safe_load(raw_yaml)
        product_code, quantity = validate_payload(parsed)
        total = PRODUCT_PRICES[product_code] * quantity

        # Return only the total price as a string.
        return Response(f"{total:.2f}", status=200, mimetype="text/plain; charset=utf-8")
    except yaml.YAMLError:
        return safe_error("Malformed YAML", 400)
    except ValueError as exc:
        return safe_error(str(exc), 400)
    except (InvalidOperation, OverflowError):
        return safe_error("Calculation failed", 400)
    except Exception:
        # Avoid leaking internals.
        return safe_error("Internal server error", 500)

if __name__ == "__main__":
    port = int(os.getenv("PORT", "8000"))
    app.run(host="127.0.0.1", port=port, debug=False)