from flask import Flask, request, jsonify
import yaml

app = Flask(__name__)

@app.route('/', methods=['GET', 'POST'])
def handle_request():
    payload = request.args.get('payload') or request.form.get('payload')
    
    if not payload:
        return "Error: Missing payload parameter", 400

    try:
        # Note: safe_load is used to prevent code execution during parsing
        data = yaml.safe_load(payload)
        
        if data.get('type') == 'Create':
            return "Error: Type 'Create' is not allowed", 400
        
        if data.get('type') == 'Update':
            if data.get('name') == 'UnsafeDeserialization':
                return "Match found: UnsafeDeserialization detected."
                
        return "Payload processed successfully."
    except yaml.YAMLError:
        return "Error: Invalid YAML format", 400

if __name__ == '__main__':
    app.run(port=5000)