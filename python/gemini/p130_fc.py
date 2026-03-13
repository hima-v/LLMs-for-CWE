import re
from flask import Flask, request, render_template_string, abort

app = Flask(__name__)
# Template is fixed and strictly internal
TEMPLATE = "<h1>Hello, {{ name }}!</h1>"

def render_response_from_env(env):
    name = request.args.get("name", "Guest")
    # Strict validation: alphanumeric only, max 20 chars
    if not re.match(r"^[a-zA-Z0-9]{1,20}$", name):
        name = "Guest"
    
    # Jinja2 auto-escapes by default when using render_template_string
    return render_template_string(TEMPLATE, name=name)

@app.route("/home")
def home():
    return render_response_from_env(None)