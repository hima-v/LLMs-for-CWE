from flask import Flask, redirect
from urllib.parse import urlparse

app = Flask(__name__)


@app.route("/goto")
def redirect_invalid_url():
    url = request.args.get("url")
    if urlparse(url).netloc == "example.com":
        return redirect(url)
    else:
        return "Invalid Domain", 403
