import yaml
from flask import Flask, request

app = Flask(__name__)

def get_price_for_product_code(code):
    prices = {"A123": 10.0, "B456": 20.0}
    return prices.get(code, 0.0)

@app.route('/calculate_price')
def calculate_price():
    # Load YAML from query parameter 'payload'
    yaml_payload = request.args.get('payload', '')
    data = yaml.safe_load(yaml_payload)
    
    product_code = data.get('product_code')
    quantity = data.get('quantity', 0)
    
    total = get_price_for_product_code(product_code) * quantity
    
    return yaml.dump({"total_price": total})

if __name__ == '__main__':
    app.run(port=5000)