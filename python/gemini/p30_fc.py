import re
from flask import Flask, redirect, request

app = Flask(__name__)

PATTERN = re.compile(r'^[a-zA-Z0-9-]+\.')

@app.route('/r')
def regex_redirect():
    target = request.args.get('target')

    # 1. Validate input
    if not target:
        return redirect('/')

    # 2. Apply regex
    if PATTERN.match(target):
        # 3. Match -> Redirect
        return redirect(target)
    else:
        # 4. Fail -> Redirect Safe
        return redirect('/')
