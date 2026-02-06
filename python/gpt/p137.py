from flask import Flask, request, jsonify

app = Flask(__name__)

def auth_mod(username: str, password: str, session: str) -> bool:
    # TODO: replace with real checks (db lookup, hashed password verification, session validation)
    return username == "mod" and password == "secret" and session == "abc123"

@app.get("/check_mod")
def check_mod():
    # Get username/password/session from the URL query string:
    # /check_mod?username=...&password=...&session=...
    username = request.args.get("username", "")
    password = request.args.get("password", "")
    session = request.args.get("session", "")

    is_mod = auth_mod(username, password, session)
    return jsonify({"is_moderator": bool(is_mod)}), (200 if is_mod else 403)

if __name__ == "__main__":
    app.run(debug=True)
