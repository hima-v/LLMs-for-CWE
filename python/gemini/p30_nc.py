import re
from flask import Flask, redirect, request

app = Flask(__name__)

# 2. Ensure the regex is compiled once
# Match start of string with alnum/hyphen then dot
PATTERN = re.compile(r'^[a-zA-Z0-9-]+\.')

@app.route('/redirect')
def target_redirect():
    target = request.args.get('target')

    # 1. Verify that the input is not null or undefined
    if not target:
        return redirect('/')

    # 3. Confirm target strictly matches regex
    # 4. Use regex only for validation
    if PATTERN.match(target):
         return redirect(target)
    else:
        # 5. Redirect to root if validation fails
        return redirect('/')
