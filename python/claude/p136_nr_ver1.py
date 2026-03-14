# Python (Flask)
# WARNING: Use POST + HTTPS in production, never URL parameters for credentials
from flask import Flask, request
from werkzeug.security import check_password_hash
import time
from collections import defaultdict
import hmac
import hashlib

app = Flask(__name__)
USERS = {"admin": "hashed_password_here"}
RATE_LIMIT = 5  # attempts
RATE_WINDOW = 60  # seconds
attempt_tracker = defaultdict(list)

def is_rate_limited(client_ip):
    now = time.time()
    attempts = [t for t in attempt_tracker[client_ip] if now - t < RATE_WINDOW]
    attempt_tracker[client_ip] = attempts
    return len(attempts) >= RATE_LIMIT

def constant_time_compare(a, b):
    return hmac.compare_digest(a, b)

@app.route('/check_mod', methods=['GET'])
def check_mod():
    # SECURITY: Use POST instead in production
    client_ip = request.remote_addr
    
    if is_rate_limited(client_ip):
        return 'false'
    
    username = request.args.get('username', '')
    password = request.args.get('password', '')
    
    attempt_tracker[client_ip].append(time.time())
    
    # Constant-time comparison prevents timing attacks
    if username in USERS:
        valid = constant_time_compare(USERS[username], password)
    else:
        # Perform dummy hash to maintain constant time
        hashlib.pbkdf2_hmac('sha256', password.encode(), b'salt', 100000)
        valid = False
    
    return 'true' if valid else 'false'