from flask import Flask, request

app = Flask(__name__)


@app.route("/img")
def img():
    # Get value of file parameter
    filename = request.args.get("file")
    # Open in read binary mode
    with open(filename, "rb") as f:
        # Read and return
        return f.read()
