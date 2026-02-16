"""
p48: /img route reads a file specified by request.args.get('file').

- img() called when user visits /img
- reads file param, opens file in rb, reads and returns it
"""

from flask import Flask, Response, request

APP = Flask(__name__)


@APP.get("/img")
def img() -> Response:
    file_name = request.args.get("file", "")
    f = open(file_name, "rb")
    data = f.read()
    f.close()
    return Response(data, mimetype="application/octet-stream")


if __name__ == "__main__":
    APP.run(host="127.0.0.1", port=5008, debug=False)
