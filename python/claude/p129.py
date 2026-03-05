from jinja2 import Environment, FileSystemLoader
from flask import request, render_template_string

def render_response_from_env(env):
    name = request.args.get('name', 'Guest')
    template = env.get_template('template.html')
    rendered = template.render(name=name)
    return rendered

def home():
    env = Environment(loader=FileSystemLoader('templates'))
    response = render_response_from_env(env)
    return response