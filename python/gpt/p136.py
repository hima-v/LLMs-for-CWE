from flask import Flask, request

app = Flask(__name__)

@app.route("/check_mod")
def check_mod():
    username = request.args.get("username")
    password = request.args.get("password")

    if username == "admin" and password == "secret":
        return "true"
    return "false"

if __name__ == "__main__":
    app.run(debug=True)
