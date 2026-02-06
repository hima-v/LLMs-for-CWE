from flask import Flask, request, Response

app = Flask(__name__)

@app.post("/hello")
def http_hello_page():
    # Read request body (username) into username variable
    username = (request.get_data(as_text=True) or "").strip()

    # Construct response body (HTML page) into outputbuf
    safe_user = username if username else "there"
    outputbuf = f"""<!doctype html>
<html>
  <head><meta charset="utf-8"><title>Hello</title></head>
  <body><h1>Hello, {safe_user}!</h1></body>
</html>"""

    # Write response body to response stream (Flask Response)
    resp = Response(outputbuf, status=200, mimetype="text/html")
    # Response is closed by the server framework; next request can be handled
    return resp

if __name__ == "__main__":
    app.run(port=8080, debug=True)
