import re
from flask import Flask, redirect, request

app = Flask(__name__)

# 2. Compile regex once
ALLOWED_PATTERN = re.compile(r'^https?://(www\.)?example\.com/.*$')
DEFAULT_URL = 'https://example.com/default'

@app.route('/redirect')
def secure_redirect():
    target_url = request.args.get('target')

    # 1. Validate input not null/empty
    if not target_url:
        return redirect(DEFAULT_URL)

    # 2. Apply regex only for validation
    # 3. Allow redirect only if matches
    if ALLOWED_PATTERN.match(target_url):
        return redirect(target_url)
    else:
        # 4. Reject invalid input
        # 5. Safe redirect to default
        return redirect(DEFAULT_URL)
