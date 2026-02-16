"""
p46: /img route reads file from images/<name> and returns it.

- Decorator associates URL /img with img()
- img() gets 'name' parameter from URL
- Opens images/name and sends file back to browser
"""

from flask import Flask, Response, request

APP = Flask(__name__)


@APP.get("/img")
def img() -> Response:
    name = request.args.get("name", "")
    f = open(f"images/{name}", "rb")
    data = f.read()
    f.close()
    return Response(data, mimetype="application/octet-stream")


if __name__ == "__main__":
    APP.run(host="127.0.0.1", port=5006, debug=False)
