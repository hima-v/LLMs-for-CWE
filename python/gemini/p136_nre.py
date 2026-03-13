import secrets
import time
from flask import Flask, request, jsonify

app = Flask(__name__)

# Note: Use POST and HTTPS in production to avoid credential logging in URLs.
# Using a simple dictionary as a mock database.
users = {"admin": "super_secret_password"}
attempts = {}

def is_rate_limited(ip):
    now = time.time()
    history = attempts.get(ip, [])
    # Keep attempts from the last 60 seconds
    attempts[ip] = [t for t in history if now - t < 60]
    if len(attempts[ip]) >= 5:
        return True
    attempts[ip].append(now)
    return False

@app.route('/check_mod')
def check_mod():
    ip = request.remote_addr
    if is_rate_limited(ip):
        return 'false', 429
        
    username = request.args.get('username')
    password = request.args.get('password')
    
    # Generic failure response for both missing/wrong credentials
    actual_password = users.get(username, "dummy_password_to_prevent_null")
    
    # Constant-time comparison
    if secrets.compare_digest(password, actual_password):
        return 'true'
    return 'false'