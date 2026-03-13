from flask import Flask, request, render_template, abort

app = Flask(__name__)

# Template is fixed, not user-chosen
TEMPLATE_NAME = "home.html"

def render_response_from_env(env):
    # Name is pulled from request and sanitized/validated
    name = request.args.get("name", "Guest")
    if not name.isalnum():
        name = "Guest"
    
    # Template source is fixed
    return render_template(TEMPLATE_NAME, name=name)

@app.route("/home")
def home():
    return render_response_from_env(None)