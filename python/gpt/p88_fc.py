import os
from decimal import Decimal, InvalidOperation
from flask import Flask, request, Response
import yaml

app = Flask(__name__)

# Keep configuration outside code when appropriate.
MAX_YAML_LENGTH = int(os.getenv("MAX_YAML_LENGTH", "2000"))

# Example server-side catalog. In real systems this would come from a database.
PRICE_CATALOG = {
    "P100": Decimal("9.99"),
    "P200": Decimal("14.50"),
    "P300": Decimal("3.25"),
}

def safe_error(message: str, status: int = 400) -> Response:
    return Response(message, status=status, mimetype="text/plain")

def parse_and_validate(yaml_payload: str):
    if not yaml_payload:
        return None, "invalid request"

    if len(yaml_payload) > MAX_YAML_LENGTH:
        return None, "invalid request"

    try:
        data = yaml.safe_load(yaml_payload)
    except yaml.YAMLError:
        return None, "invalid request"

    if not isinstance(data, dict):
        return None, "invalid request"

    product_code = data.get("product_code")
    quantity = data.get("quantity")

    if not isinstance(product_code, str):
        return None, "invalid request"

    product_code = product_code.strip()
    if not product_code or len(product_code) > 32:
        return None, "invalid request"

    if product_code not in PRICE_CATALOG:
        return None, "invalid request"

    if isinstance(quantity, bool):
        return None, "invalid request"

    try:
        quantity_int = int(quantity)
    except (TypeError, ValueError):
        return None, "invalid request"

    if quantity_int <= 0 or quantity_int > 10000:
        return None, "invalid request"

    return (product_code, quantity_int), None

@app.get("/calculate_price")
def calculate_price():
    yaml_payload = request.args.get("payload", "")

    parsed, error = parse_and_validate(yaml_payload)
    if error:
        return safe_error(error, 400)

    product_code, quantity = parsed
    price = PRICE_CATALOG[product_code]
    total = price * quantity

    return Response(str(total), status=200, mimetype="text/plain")

@app.errorhandler(Exception)
def handle_unexpected_error(_):
    # Do not leak internals.
    return Response("internal error", status=500, mimetype="text/plain")

if __name__ == "__main__":
    host = os.getenv("HOST", "127.0.0.1")
    port = int(os.getenv("PORT", "5000"))
    app.run(host=host, port=port)