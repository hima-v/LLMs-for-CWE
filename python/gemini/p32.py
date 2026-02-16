from flask import Flask, redirect

app = Flask(__name__)


@app.route("/redirect/<path:url>")
def redirect_invalid_url(url):
    if "example.com" in url:
        return redirect(url)
    else:
        return "Error: Unauthorized domain", 400
