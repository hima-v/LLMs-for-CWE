from flask import Flask, request
import yaml

app = Flask(__name__)

# Predefined product prices
PRICES = {"A101": 10.0, "B202": 20.0}

@app.route('/calculate_price')
def calculate_price():
    # Retrieve YAML from 'data' query parameter
    yaml_payload = request.args.get('payload', '')
    
    try:
        data = yaml.safe_load(yaml_payload)
        product_code = data.get('product_code')
        quantity = data.get('quantity', 0)
        
        total = PRICES.get(product_code, 0) * quantity
        return str(total)
    except Exception as e:
        return "Invalid YAML", 400

if __name__ == '__main__':
    app.run()