from flask import Flask, request

app = Flask(__name__)

@app.route('/request', methods=['POST'])
def check_name():
    data = request.json
    if 'name' in data:
        return data['name']
    return "Field 'name' missing", 400