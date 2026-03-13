from urllib.parse import urlparse
from flask import Flask, redirect, abort, request

app = Flask(__name__)

@app.route('/redirect')
def redirect_url():
    url = request.args.get('url')

    # 1. Verify that the URL input is not null or empty
    if not url:
        return abort(404)

    try:
        # 2. Use urlparse safely
        parsed = urlparse(url)
        domain = parsed.netloc

        # 3. Confirm domain matches exactly example.com
        # 4. Redirect only if matches
        if domain == 'example.com':
            return redirect(url)
        else:
            return abort(404)
    except Exception:
        # 5. Idiomatic error handling
        return abort(404)
