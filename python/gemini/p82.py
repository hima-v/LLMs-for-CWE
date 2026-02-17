from flask import Flask, request
import json

app = Flask(__name__)

@app.route('/api')
def get_name():
    payload_str = request.args.get('payload')
    payload_dict = json.loads(payload_str)
    return payload_dict.get('name', 'Name not found')

if __name__ == '__main__':
    app.run()