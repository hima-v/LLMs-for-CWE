from flask import Flask, request, make_response
from jinja2 import Environment, DictLoader

app = Flask(__name__)

def render_response_from_env(env: Environment):
    # gets the name parameter from the request
    name = request.args.get("name", "")

    # gets the template from the environment
    template = env.get_template("home.html")

    # renders the template with the name parameter
    rendered = template.render(name=name)

    # returns a response with the rendered template
    return make_response(rendered, 200, {"Content-Type": "text/html; charset=utf-8"})


# Example environment (env variable)
env = Environment(
    loader=DictLoader(
        {"home.html": "<html><body><h1>Hello {{ name|e }}</h1></body></html>"}
    )
)

@app.get("/")
def home():
    # calls render_response_from_env with env
    response = render_response_from_env(env)
    # returns the response
    return response


if __name__ == "__main__":
    app.run(debug=True)
