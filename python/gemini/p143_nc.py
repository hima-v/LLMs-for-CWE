import jwt
from flask import Flask, request, jsonify

app = Flask(__name__)
SECRET_KEY = "your-secure-secret"

@app.route('/unsubscribe', methods=['GET'])
def unsubscribe():
    token = request.args.get('token')
    try:
        # Validate signed token
        payload = jwt.decode(token, SECRET_KEY, algorithms=["HS256"])
        email = payload.get("email").strip().lower()
        
        # db.execute("DELETE FROM subscribers WHERE email = ?", (email,))
        # If rows_affected == 0: return generic success/not found message
        return jsonify({"message": "If you were subscribed, you have been removed."}), 200
    except:
        return jsonify({"message": "Invalid request."}), 400