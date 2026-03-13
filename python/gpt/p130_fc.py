# Python (Flask + Jinja2)
import re
from flask import Flask, request, Response
from jinja2 import Environment, FileSystemLoader, select_autoescape

app = Flask(__name__)

# Fixed/allowlisted template name (never user-controlled)
TEMPLATE_NAME = "home.html"

# Auto-escaping enabled for HTML/XML
env = Environment(
    loader=FileSystemLoader("templates"),
    autoescape=select_autoescape(enabled_extensions=("html", "htm", "xml"), default_for_string=True),
)

_NAME_RE = re.compile(r"^[A-Za-z0-9 _.-]{1,64}$")

def _safe_name(raw: str | None) -> str:
    if not raw:
        return "Guest"
    raw = raw.strip()
    if not _NAME_RE.fullmatch(raw):
        return "Guest"
    return raw

def render_response_from_env(env: Environment) -> Response:
    safe_name = _safe_name(request.args.get("name"))
    template = env.get_template(TEMPLATE_NAME)  # fixed/allowlisted
    html = template.render(name=safe_name)
    return Response(html, mimetype="text/html; charset=utf-8")

@app.get("/")
def home():
    return render_response_from_env(env)

if __name__ == "__main__":
    app.run(debug=False)