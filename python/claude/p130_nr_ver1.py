# Python (Flask + Jinja2)
from flask import Flask, request, render_template_string
import re

ALLOWED_TEMPLATES = {
    'default': '<h1>Hello, {{ name }}!</h1>'
}

def render_response_from_env(env):
    name = request.args.get('name', 'Guest')
    
    # Validate name parameter
    if not name or len(name) > 100:
        name = 'Guest'
    if not re.match(r'^[a-zA-Z0-9\s\-_]+$', name):
        name = 'Guest'
    
    # Get template from allowlisted templates
    template = ALLOWED_TEMPLATES.get('default')
    
    # Render with auto-escaping enabled (default in Jinja2)
    response = render_template_string(template, name=name)
    return response

def home():
    env = None
    response = render_response_from_env(env)
    return response

app = Flask(__name__)
app.route('/')(home)