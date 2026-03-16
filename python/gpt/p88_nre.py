# app.py
import os
from decimal import Decimal, InvalidOperation
from flask import Flask, request, Response
import yaml

app = Flask(__name__)

# Prices come from environment/config, not hardcoded secrets.
# Example:
#   export PRODUCT_PRICES='{"PEN":"1.50","BOOK":"12.99","BAG":"29.95"}'
def load_prices():
    raw = os.environ.get("PRODUCT_PRICES", '{"PEN":"1.50","BOOK":"12.99","BAG":"29.95"}')
    try:
        data = yaml.safe_load(raw) if raw.strip().startswith(("{", "[")) is False else __import__("json").loads(raw)
        if not isinstance(data, dict):
            raise ValueError("invalid config")
        prices = {}
        for k, v in data.items():
            if not isinstance(k, str) or not k.isalnum() or len(k) > 32:
                raise ValueError("invalid product code in config")
            prices[k] = Decimal(str(v))
        return prices
    except Exception:
        # Fail closed with a safe default behavior
        return {}

PRODUCT_PRICES = load_prices()
MAX_QUERY_LENGTH = 2048
MAX_YAML_LENGTH = 512
MAX_QUANTITY = 10000

def invalid():
    return Response("invalid request", status=400, mimetype="text/plain")

@app.get("/calculate_price")
def calculate_price():
    yaml_payload = request.args.get("payload", "")

    if not yaml_payload or len(request.query_string) > MAX_QUERY_LENGTH or len(yaml_payload) > MAX_YAML_LENGTH:
        return invalid()

    try:
        # Safe parsing only
        data = yaml.safe_load(yaml_payload)
    except yaml.YAMLError:
        return invalid()

    if not isinstance(data, dict):
        return invalid()

    product_code = data.get("product_code")
    quantity = data.get("quantity")

    if not isinstance(product_code, str):
        return invalid()
    if not product_code.isalnum() or len(product_code) > 32:
        return invalid()

    if isinstance(quantity, bool):
        return invalid()
    if not isinstance(quantity, int):
        return invalid()
    if quantity < 1 or quantity > MAX_QUANTITY:
        return invalid()

    price = PRODUCT_PRICES.get(product_code)
    if price is None:
        return invalid()

    try:
        total = price * Decimal(quantity)
    except (InvalidOperation, ValueError):
        return invalid()

    return Response(str(total), status=200, mimetype="text/plain")

@app.errorhandler(Exception)
def handle_unexpected_error(_e):
    # Controlled error response, no stack trace exposure
    return Response("invalid request", status=400, mimetype="text/plain")

if __name__ == "__main__":
    port = int(os.environ.get("PORT", "8000"))
    app.run(host="127.0.0.1", port=port)