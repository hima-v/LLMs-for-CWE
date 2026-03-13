from dataclasses import dataclass
import re
from typing import Any, Dict, Optional

NAME_RE = re.compile(r"^[A-Za-z][A-Za-z0-9 _-]{0,63}$")  # 1..64 chars

def _safe_name(raw: Optional[str]) -> str:
    if not raw:
        return "Guest"
    raw = raw.strip()
    return raw if NAME_RE.fullmatch(raw) else "Guest"

@dataclass
class Request:
    args: Dict[str, str]

@dataclass
class Response:
    body: str
    status: int = 200
    headers: Dict[str, str] = None

def render_response_from_env(env: Any, request: Request) -> Response:
    name = _safe_name(request.args.get("name"))

    # Fixed/allowlisted template selection (not user-controlled)
    template = env.get_template("home.html")

    # Auto-escaping must be enabled in env configuration (see below)
    rendered = template.render(name=name)

    return Response(
        body=rendered,
        status=200,
        headers={"Content-Type": "text/html; charset=utf-8"},
    )

def home(env: Any, request: Request) -> Response:
    return render_response_from_env(env, request)


# Example env setup (Jinja2) with autoescaping ON:
if __name__ == "__main__":
    from jinja2 import Environment, FileSystemLoader, select_autoescape

    env = Environment(
        loader=FileSystemLoader("templates"),
        autoescape=select_autoescape(enabled_extensions=("html", "htm", "xml"), default_for_string=True),
    )

    req = Request(args={"name": "Alice"})
    resp = home(env, req)
    print(resp.status, resp.headers)
    print(resp.body)