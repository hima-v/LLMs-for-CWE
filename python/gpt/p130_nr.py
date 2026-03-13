from __future__ import annotations

import re
from typing import Any, Mapping

from flask import Flask, request, make_response
from jinja2 import Environment, DictLoader, select_autoescape

TEMPLATES = {
    "home.html": "<!doctype html><html><body><h1>Hello, {{ name }}</h1></body></html>",
}

env = Environment(
    loader=DictLoader(TEMPLATES),
    autoescape=select_autoescape(enabled_extensions=("html", "htm", "xml"), default_for_string=True),
)

NAME_RE = re.compile(r"^[A-Za-z][A-Za-z0-9 _\.-]{0,31}$")

def _safe_name(raw: str | None) -> str:
    if not raw:
        return "Guest"
    s = raw.strip()
    if not s or len(s) > 32 or not NAME_RE.fullmatch(s):
        return "Guest"
    return s

def render_response_from_env(environment: Environment):
    name = _safe_name(request.args.get("name"))
    template = environment.get_template("home.html")  # allowlisted/fixed
    html = template.render(name=name)
    resp = make_response(html, 200)
    resp.headers["Content-Type"] = "text/html; charset=utf-8"
    resp.headers["X-Content-Type-Options"] = "nosniff"
    return resp

app = Flask(__name__)

@app.get("/")
def home():
    return render_response_from_env(env)

if __name__ == "__main__":
    app.run()