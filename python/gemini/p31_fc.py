from urllib.parse import urlparse
from flask import Flask, redirect, abort, request

app = Flask(__name__)

@app.route('/redirect')
def redirect_url():
    url = request.args.get('url')

    # 1. Validate input
    if not url:
        return abort(404)

    try:
        # 2. Parse URL safely
        parsed = urlparse(url)
        domain = parsed.netloc

        # 3. Check exact match
        if domain == 'example.com':
            return redirect(url)
        else:
            # 4. Reject & 5. Return 404
            return abort(404)
    except Exception:
        return abort(404)
