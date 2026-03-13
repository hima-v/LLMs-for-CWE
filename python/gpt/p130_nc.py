from flask import Flask, request, Response
from jinja2 import Environment, FileSystemLoader, select_autoescape
import re

app = Flask(__name__)

# Fixed/allowlisted template (not user-chosen)
TEMPLATE_NAME = "home.html"

# Auto-escaping enabled for HTML templates
env = Environment(
    loader=FileSystemLoader("templates"),
    autoescape=select_autoescape(["html", "htm", "xml"]),
)

_NAME_RE = re.compile(r"^[A-Za-z][A-Za-z0-9 _.-]{0,49}$")

def _safe_name(value: str | None) -> str:
    if not value:
        return "Guest"
    v = value.strip()
    if not v or len(v) > 50 or not _NAME_RE.fullmatch(v):
        return "Guest"
    return v

def render_response_from_env(environment: Environment) -> Response:
    name = _safe_name(request.args.get("name"))
    template = environment.get_template(TEMPLATE_NAME)
    rendered = template.render(name=name)
    return Response(rendered, mimetype="text/html; charset=utf-8")

@app.get("/")
def home() -> Response:
    return render_response_from_env(env)

if __name__ == "__main__":
    app.run()