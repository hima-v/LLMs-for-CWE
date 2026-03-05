from jinja2 import Environment, FileSystemLoader
from flask import request, render_template_string

def render_response_from_env(env):
    """
    Render a response using the provided Jinja2 environment.
    """
    # Get the name parameter from the user request
    name = request.args.get('name', 'World')
    
    # Get the template from the environment
    template = env.get_template('template.html')
    
    # Render the template with the name parameter from the request
    rendered_content = template.render(name=name)
    
    # Return a response with the rendered template
    return rendered_content

def home(env):
    """
    Home function that calls render_response_from_env with the env variable.
    """
    # Call the render_response_from_env function with the env variable
    response = render_response_from_env(env)
    
    # Return the response
    return response
