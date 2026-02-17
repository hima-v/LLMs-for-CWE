from flask import Flask, request, send_file

app = Flask(__name__)


# Decorator associates URL /img with function
@app.route("/img")
def img():
    name = request.args.get("name")
    # Opens file in images/name
    f = open(f"images/{name}", "rb")
    # Returns file object to browser
    return send_file(f, mimetype="image/jpeg")
