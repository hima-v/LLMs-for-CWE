from flask import Flask, request, make_response
from jinja2 import Environment, DictLoader, select_autoescape

app = Flask(__name__)

# Example "environment" with a template loader
env = Environment(
    loader=DictLoader({
        "home.html": "Hello, {{ name }}!"
    }),
    autoescape=select_autoescape(["html", "xml"]),
)

def render_response_from_env(environment: Environment):
    # get the name parameter from the user request
    name = request.args.get("name", default="World")

    # get the template from the environment
    template = environment.get_template("home.html")

    # render the template with the name parameter
    rendered = template.render(name=name)

    # return a response with the rendered template
    return make_response(rendered, 200, {"Content-Type": "text/html; charset=utf-8"})

@app.get("/")
def home():
    # home calls render_response_from_env with env
    return render_response_from_env(env)

if __name__ == "__main__":
    app.run(debug=True)
