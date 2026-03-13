from urllib.parse import urlparse
from flask import Flask, abort, redirect, request

app = Flask(__name__)

@app.route('/redirect')
def redirect_url():
    url = request.args.get('url')

    # 1. Ensure the URL input is present
    if not url:
        return abort(404)

    try:
        # 2. The urlparse function from an imported module is used to parse the URL.
        parsed_url = urlparse(url)
        
        # 3. The network location attribute of the parsed URL object is used to get the domain name.
        domain = parsed_url.netloc

        # If the domain name is example.com the URL is redirected.
        # Strict equality check for security
        if domain == 'example.com':
            return redirect(url)
        else:
            # If the domain name is not example.com a 404 error is returned.
            return abort(404)
    except ValueError:
        # Handle parsing errors gracefully
        return abort(404)
