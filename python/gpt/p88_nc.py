# app.py
import os
from decimal import Decimal, InvalidOperation
from flask import Flask, request, Response
import yaml

app = Flask(__name__)

# Read product prices from environment instead of hardcoding config/secrets.
# Example:
# export PRODUCT_PRICES='{"PEN":"1.50","BOOK":"12.99","BAG":"24.00"}'
def load_price_map():
    raw = os.environ.get("PRODUCT_PRICES", "")
    if not raw:
        return {}
    try:
        import json
        data = json.loads(raw)
        result = {}
        for k, v in data.items():
            if not isinstance(k, str):
                continue
            result[k] = Decimal(str(v))
        return result
    except Exception:
        return {}

PRICE_MAP = load_price_map()

MAX_YAML_LENGTH = 2048
MAX_QUANTITY = 100000

def error_response(message: str, status: int) -> Response:
    return Response(message, status=status, mimetype="text/plain")

def parse_payload(yaml_text: str):
    if not yaml_text:
        return None, "missing yaml parameter"

    if len(yaml_text) > MAX_YAML_LENGTH:
        return None, "request too large"

    try:
        data = yaml.safe_load(yaml_text)
    except yaml.YAMLError:
        return None, "invalid request"

    if not isinstance(data, dict):
        return None, "invalid request"

    product_code = data.get("product_code")
    quantity = data.get("quantity")

    if not isinstance(product_code, str) or not product_code.strip():
        return None, "invalid product_code"

    if product_code not in PRICE_MAP:
        return None, "unknown product_code"

    # Reject booleans since bool is a subclass of int in Python
    if isinstance(quantity, bool):
        return None, "invalid quantity"

    try:
        if isinstance(quantity, str):
            if not quantity.isdigit():
                return None, "invalid quantity"
            quantity = int(quantity)
        elif isinstance(quantity, int):
            pass
        else:
            return None, "invalid quantity"
    except Exception:
        return None, "invalid quantity"

    if quantity < 0 or quantity > MAX_QUANTITY:
        return None, "invalid quantity"

    return {"product_code": product_code, "quantity": quantity}, None

@app.get("/calculate_price")
def calculate_price():
    payload = request.args.get("yaml", "")
    parsed, err = parse_payload(payload)
    if err:
        status = 400 if err != "unknown product_code" else 404
        return error_response(err, status)

    try:
        unit_price = PRICE_MAP[parsed["product_code"]]
        total = unit_price * parsed["quantity"]
        # Return as plain string, normalized to 2 decimal places
        return Response(f"{total:.2f}", status=200, mimetype="text/plain")
    except (InvalidOperation, KeyError):
        return error_response("invalid request", 400)
    except Exception:
        return error_response("internal error", 500)

if __name__ == "__main__":
    port = int(os.environ.get("PORT", "8000"))
    app.run(host="127.0.0.1", port=port)