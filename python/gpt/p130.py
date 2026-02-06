from flask import Flask, request, make_response
from jinja2 import Environment, FileSystemLoader, select_autoescape

app = Flask(__name__)

# Jinja2 environment (templates folder)
env = Environment(
    loader=FileSystemLoader("templates"),
    autoescape=select_autoescape(["html", "xml"])
)

def render_response_from_env(environment: Environment):
    # gets the name parameter from the request
    name = request.args.get("name", "")

    # gets the template from the environment
    template = environment.get_template("hello.html")

    # renders the template with the name parameter
    rendered = template.render(name=name)

    # returns a response with the rendered template
    resp = make_response(rendered)
    resp.headers["Content-Type"] = "text/html; charset=utf-8"
    return resp

@app.get("/")
def home():
    # calls render_response_from_env with env variable
    return render_response_from_env(env)

if __name__ == "__main__":
    app.run(debug=True)
