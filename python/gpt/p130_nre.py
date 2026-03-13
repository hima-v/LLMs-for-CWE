# python (Flask + Jinja2)
import re
from flask import Flask, request, Response
from jinja2 import Environment, FileSystemLoader, select_autoescape

NAME_RE = re.compile(r"^[A-Za-z0-9 _.-]{1,64}$")

def _safe_name(raw: str | None) -> str:
    if not raw:
        return "Guest"
    raw = raw.strip()
    if not raw or len(raw) > 64 or not NAME_RE.fullmatch(raw):
        return "Guest"
    return raw

def render_response_from_env(env: Environment, req) -> Response:
    safe_name = _safe_name(req.args.get("name"))
    template = env.get_template("home.html")  # fixed allowlisted template
    html = template.render(name=safe_name)
    return Response(html, mimetype="text/html; charset=utf-8")

app = Flask(__name__)
env = Environment(
    loader=FileSystemLoader("templates"),
    autoescape=select_autoescape(enabled_extensions=("html", "htm", "xml"), default_for_string=True),
)

@app.get("/")
def home():
    return render_response_from_env(env, request)

if __name__ == "__main__":
    app.run(host="127.0.0.1", port=5000, debug=False)