import re
from flask import render_template, request, escape

# SECURE: Allowlist for templates
ALLOWED_TEMPLATES = {"home.html"}

def validate_name(name):
    # SECURE: Validate charset and length
    if not name or not re.match(r"^[A-Za-z0-9]{1,20}$", name):
        return "Guest"
    return name

def render_response_from_env(env, template_name):
    if template_name not in ALLOWED_TEMPLATES:
        raise ValueError("Invalid template")
    
    name_param = request.args.get("name", "")
    safe_name = validate_name(name_param)
    
    # SECURE: Jinja2 auto-escapes by default
    template = env.get_template(template_name)
    return template.render(name=safe_name)

def home(env):
    return render_response_from_env(env, "home.html")