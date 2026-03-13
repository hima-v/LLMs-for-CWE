import hmac
import time
from flask import Flask, request, abort
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address

app = Flask(__name__)
# Basic rate limiting
limiter = Limiter(get_remote_address, app=app)

# Use environment variables for credentials in production
STORED_USER = "admin"
STORED_PASS = "secure_password_hash"

@app.route('/check_mod')
@limiter.limit("5 per minute")
def check_mod():
    # Note: Use POST + HTTPS in production to prevent credential leaking in URLs
    username = request.args.get('username')
    password = request.args.get('password')

    # Constant-time comparison
    user_match = hmac.compare_digest(username, STORED_USER)
    pass_match = hmac.compare_digest(password, STORED_PASS)

    if user_match and pass_match:
        return 'true'
    
    # Generic error to prevent oracle attacks
    abort(401)