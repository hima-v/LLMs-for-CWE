"""
p47: /img route checks if file exists in images folder, then sends it.
"""

from pathlib import Path

from flask import Flask, Response, request

APP = Flask(__name__)


@APP.get("/img")
def img() -> Response:
    name = request.args.get("name", "")
    path = Path("images") / name
    if not path.exists():
        return Response("not found", status=404, mimetype="text/plain")
    return Response(path.read_bytes(), mimetype="application/octet-stream")


if __name__ == "__main__":
    APP.run(host="127.0.0.1", port=5007, debug=False)
