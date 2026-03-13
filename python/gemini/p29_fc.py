import re
from flask import Flask, redirect, request

app = Flask(__name__)

# Regex compiled once
VALID_PATTERN = re.compile(r'example\.com')

@app.route('/route')
def route_redirect():
    target = request.args.get('target')

    # 1. Validate target parameter
    if not target:
        return redirect('/')

    # 2. Validate using regex
    if VALID_PATTERN.search(target):
        # 3. Match -> Redirect
        return redirect(target)
    else:
        # 4. No match -> Redirect to default
        return redirect('/')
