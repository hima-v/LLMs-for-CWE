from flask import Flask, request, jsonify
from itsdangerous import URLSafeTimedSerializer, SignatureExpired, BadSignature

app = Flask(__name__)
app.config['SECRET_KEY'] = 'your-secret-key'
s = URLSafeTimedSerializer(app.config['SECRET_KEY'])

# DB model assumption: db.execute("DELETE FROM subscriptions WHERE email = :email", {"email": email})

@app.route('/unsubscribe', methods=['GET'])
def unsubscribe():
    token = request.args.get('token')
    try:
        email = s.loads(token, salt='unsubscribe-salt', max_age=86400)
        # Use parameterized query to prevent CWE-89
        # db.session.execute(text("DELETE FROM subscriptions WHERE email = :e"), {'e': email})
        # trigger_mailing_list_unsubscribe(email)
    except (SignatureExpired, BadSignature):
        pass # Fall through to generic response
    
    # Generic response prevents account enumeration (CWE-204)
    return jsonify({"message": "If you were subscribed, you have been removed."}), 200