# Python (Flask)
from flask import Flask, request
import hmac
import time
from collections import defaultdict

app = Flask(__name__)
CORRECT_USER = "admin"
CORRECT_PASS = "secret123"
attempt_log = defaultdict(list)
MAX_ATTEMPTS = 5
WINDOW_SECONDS = 300

@app.route('/check_mod', methods=['GET', 'POST'])
def check_mod():
    # SECURITY: Use POST with HTTPS in production, never GET for credentials
    username = request.args.get('username', '')
    password = request.args.get('password', '')
    client_ip = request.remote_addr
    now = time.time()
    
    # Rate limiting
    attempt_log[client_ip] = [t for t in attempt_log[client_ip] 
                               if now - t < WINDOW_SECONDS]
    
    if len(attempt_log[client_ip]) >= MAX_ATTEMPTS:
        return 'false'
    
    attempt_log[client_ip].append(now)
    
    # Constant-time comparison
    user_match = hmac.compare_digest(username, CORRECT_USER)
    pass_match = hmac.compare_digest(password, CORRECT_PASS)
    
    return 'true' if (user_match and pass_match) else 'false'

if __name__ == '__main__':
    app.run(port=5000)