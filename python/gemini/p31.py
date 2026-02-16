from flask import Flask, redirect, abort
from urllib.parse import urlparse

app = Flask(__name__)


def redirect_url(url):
    parsed_url = urlparse(url)
    domain_name = parsed_url.netloc
    if domain_name == "example.com":
        return redirect(url)
    else:
        abort(404)
