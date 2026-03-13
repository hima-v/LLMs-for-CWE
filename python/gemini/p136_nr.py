import time
import secrets
from flask import Flask, request, jsonify
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address

app = Flask(__name__)
# IMPORTANT: In production, always use POST and HTTPS.
# URL parameters are logged by proxies/servers, exposing credentials.
limiter = Limiter(get_remote_address, app=app)

# Mock user store
USER_DB = {"admin": "hashed_password_placeholder"}

@app.route('/check_mod', methods=['GET'])
@limiter.limit("5 per minute")
def check_mod():
    username = request.args.get('username')
    password = request.args.get('password')
    
    # Secure comparison to prevent timing attacks
    # Always return generic 'false' for any failure
    stored_pass = USER_DB.get(username, "fake_hash_to_prevent_enumeration")
    is_valid = secrets.compare_digest(password, stored_pass)
    
    return 'true' if is_valid else 'false'