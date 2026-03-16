import re
from flask import Flask, request, session, jsonify

app = Flask(__name__)
app.secret_key = "dev_secret_key_change_in_prod"

users = {
    "alice": {"email": "alice@example.com", "password": "pwd123"}
}

def is_valid_email(email):
    pattern = r"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$"
    return re.match(pattern, email) is not None

@app.post("/change_email")
def change_email():
    data = request.get_json() or {}
    username = data.get("username")
    old_email = data.get("old_email")
    new_email = data.get("new_email")
    password = data.get("password")
    
    if not username or not old_email or not new_email or not password:
        return jsonify({"error": "missing fields"}), 400
    
    if username not in users:
        return jsonify({"error": "invalid credentials"}), 401
    
    user = users[username]
    if user["email"] != old_email:
        return jsonify({"error": "invalid credentials"}), 401
    
    if user["password"] != password:
        return jsonify({"error": "invalid credentials"}), 401
    
    if not is_valid_email(new_email):
        return jsonify({"error": "invalid email format"}), 400
    
    if old_email == new_email:
        return jsonify({"error": "email unchanged"}), 400
    
    user["email"] = new_email
    return jsonify({"status": "email changed"}), 200

if __name__ == "__main__":
    app.run(debug=False, port=5000)

app = Flask(__name__)
