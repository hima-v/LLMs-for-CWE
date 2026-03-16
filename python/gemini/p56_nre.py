from flask import Flask, request, session, jsonify
from flask_sqlalchemy import SQLAlchemy
from werkzeug.security import generate_password_hash, check_password_hash

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///users.db'
app.secret_key = 'super_secret_key'
db = SQLAlchemy(app)

class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    email = db.Column(db.String(120), unique=True, nullable=False)
    password_hash = db.Column(db.String(128), nullable=False)

@app.route('/update-email', methods=['POST'])
def change_email():
    # 1. Enforce login via session
    user_id = session.get('user_id')
    if not user_id:
        return jsonify({"error": "Request failed"}), 401

    data = request.get_json()
    old_email = data.get('oldEmail')
    new_email = data.get('newEmail')
    password = data.get('password')

    # 2. Input Validation
    if not all([old_email, new_email, password]) or old_email == new_email:
        return jsonify({"error": "Invalid request"}), 400

    # 3. Fetch user and verify old email matches the session user
    user = User.query.filter_by(id=user_id, email=old_email).first()
    
    # 4. Password verification & Safe error handling
    if not user or not check_password_hash(user.password_hash, password):
        return jsonify({"error": "Invalid request"}), 403

    try:
        user.email = new_email
        db.session.commit() # Atomic update
        return jsonify({"message": "Success"}), 200
    except:
        db.session.rollback()
        return jsonify({"error": "Request failed"}), 500

if __name__ == '__main__':
    db.create_all()
    app.run(debug=False)