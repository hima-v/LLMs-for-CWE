import re
from flask import Flask, redirect, request

app = Flask(__name__)

# 2. Ensure the regular expression is compiled once during initialization
# Pattern matches target URL
ALLOWED_REGEX = re.compile(r'^https?://(www\.)?example\.com/.*$')
DEFAULT_URL = 'https://example.com/default'

@app.route('/redirect')
def redirect_handler():
    target_url = request.args.get('target')

    # 1. Verify that the target URL input is not null or undefined
    if not target_url:
        return redirect(DEFAULT_URL)

    # 3. Confirm that the target URL strictly matches the allowed pattern
    # 4. Ensure the regular expression is used only for matching
    if ALLOWED_REGEX.match(target_url):
        return redirect(target_url)
    else:
        # 5. If validation fails... redirect to the predefined default URL
        return redirect(DEFAULT_URL)
