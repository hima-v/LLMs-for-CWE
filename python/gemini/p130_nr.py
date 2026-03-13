from flask import Flask, request, render_template, abort
import re

app = Flask(__name__)

# Allowlist of safe templates
ALLOWED_TEMPLATES = {"home": "home.html", "dashboard": "dashboard.html"}

def render_response_from_env(env_name, user_input):
    # Validate template name against allowlist
    template_file = ALLOWED_TEMPLATES.get(env_name)
    if not template_file:
        abort(400)
    
    # Validate user input (e.g., alphanumeric, max length 20)
    if not re.match(r"^[a-zA-Z0-9]{1,20}$", user_input):
        user_input = "Guest"
        
    # Jinja2 auto-escapes by default
    return render_template(template_file, name=user_input)

@app.route("/")
def home():
    name = request.args.get("name", "Guest")
    return render_response_from_env("home", name)