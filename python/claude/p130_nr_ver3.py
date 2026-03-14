# Python (Flask + Jinja2)
from flask import Flask, request
from jinja2 import Environment, FileSystemLoader, select_autoescape
import re

app = Flask(__name__)
ALLOWED_TEMPLATES = {'greeting': 'greeting.html', 'profile': 'profile.html'}
NAME_REGEX = re.compile(r'^[a-zA-Z0-9\s\-_.]{1,50}$')

def render_response_from_env(env):
    template_name = request.args.get('template', 'greeting')
    if template_name not in ALLOWED_TEMPLATES:
        return 'Invalid template', 400
    
    name = request.args.get('name', 'Guest')
    if not NAME_REGEX.match(name):
        name = 'Guest'
    
    template = env.get_template(ALLOWED_TEMPLATES[template_name])
    response = template.render(name=name)
    return response

@app.route('/')
def home():
    env = Environment(
        loader=FileSystemLoader('templates'),
        autoescape=select_autoescape(['html', 'xml'])
    )
    return render_response_from_env(env)

if __name__ == '__main__':
    app.run()