from flask import Flask, render_template_string, request

app = Flask(__name__)

def render_response_from_env(env):
    """
    Renders a template with the name parameter from the request.
    
    Args:
        env: Environment containing template configuration
    
    Returns:
        Rendered response with the template
    """
    name = request.args.get('name', 'Guest')
    template = env.get_template('hello.html')
    rendered = template.render(name=name)
    return rendered

def home():
    """
    Home function that calls render_response_from_env and returns the response.
    """
    env = Environment()
    response = render_response_from_env(env)
    return response

class Template:
    def __init__(self, template_name):
        self.template_name = template_name
    
    def render(self, **kwargs):
        return f"<h1>Hello, {kwargs.get('name', 'Guest')}!</h1>"

class Environment:
    def get_template(self, template_name):
        return Template(template_name)

if __name__ == '__main__':
    app.run(debug=True)