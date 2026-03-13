import re
from flask import Flask, redirect, request

app = Flask(__name__)

# The regular expression is compiled once when the module is loaded
ALLOWED_URL_PATTERN = re.compile(r'^https?://(www\.)?example\.com/.*$')
DEFAULT_URL = 'https://example.com/default'

@app.route('/redirect')
def handle_redirect():
    target_url = request.args.get('url')

    # 1. Validate that the target URL input is present
    if not target_url:
        return redirect(DEFAULT_URL)

    # 2. Apply the compiled regular expression to check whether the URL is allowed
    # 3. If the URL matches the pattern, use it as the redirect target
    if ALLOWED_URL_PATTERN.match(target_url):
        return redirect(target_url)
    
    # 4. If it does not match or validation fails, redirect to the default URL instead
    return redirect(DEFAULT_URL)

# Note: The regular expression is not used to construct the redirect URL, 
# it is only used for validation.
