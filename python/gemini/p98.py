from flask import Flask, request, jsonify
from flask_bcrypt import Bcrypt
# Assuming a database connector like SQLAlchemy is set up

app = Flask(__name__)
bcrypt = Bcrypt(app)

@app.route('/register', methods=['POST'])
def register():
    data = request.get_json()
    username = data.get('username')
    password = data.get('password')

    # Hash the password
    # bcyrpt.generate_password_hash handles the salt automatically
    hashed_password = bcrypt.generate_password_hash(password).decode('utf-8')

    # Logic to insert into DB (e.g., db.session.add(User(username, hashed_password)))
    return jsonify({"message": "User registered successfully"}), 201