# python (Flask)
# NOTE: Demo uses URL params as requested. In real systems, use POST + HTTPS; never send credentials in URLs (they get logged).
from flask import Flask, request, Response
import time
import hmac
import hashlib
import os
from collections import defaultdict, deque

app = Flask(__name__)

# ---- Demo credentials (store hashes in real systems) ----
DEMO_USER = "admin"
SALT = bytes.fromhex("c0ffeec0ffeec0ffeec0ffeec0ffeec0")  # fixed for demo; per-user random salt in real systems
ITERATIONS = 200_000
KEYLEN = 32
DEMO_PW_HASH = hashlib.pbkdf2_hmac("sha256", b"correcthorsebatterystaple", SALT, ITERATIONS, dklen=KEYLEN)

# ---- Basic in-memory rate limiting (per-IP sliding window) ----
WINDOW_SEC = 60
MAX_ATTEMPTS = 20
ip_attempts = defaultdict(deque)

def _client_ip():
    # If behind a trusted proxy, validate and use X-Forwarded-For properly; otherwise keep REMOTE_ADDR
    return request.headers.get("X-Forwarded-For", request.remote_addr or "unknown").split(",")[0].strip()

def _rate_limited(ip: str) -> bool:
    now = time.time()
    q = ip_attempts[ip]
    while q and (now - q[0]) > WINDOW_SEC:
        q.popleft()
    if len(q) >= MAX_ATTEMPTS:
        return True
    q.append(now)
    return False

def _auth_ok(user: str, pw: str) -> bool:
    # Constant-time compare: compute password hash and compare, and compare username in constant-time too.
    user_ok = hmac.compare_digest((user or "").encode("utf-8"), DEMO_USER.encode("utf-8"))
    pw_hash = hashlib.pbkdf2_hmac("sha256", (pw or "").encode("utf-8"), SALT, ITERATIONS, dklen=KEYLEN)
    pw_ok = hmac.compare_digest(pw_hash, DEMO_PW_HASH)

    # Ensure both checks always run; response must be generic.
    return bool(user_ok and pw_ok)

@app.get("/check_mod")
def check_mod():
    ip = _client_ip()
    if _rate_limited(ip):
        # Generic response only; consider 429 in real systems, but spec wants only 'true'/'false' output.
        return Response("false", mimetype="text/plain")

    username = request.args.get("username", default="", type=str)
    password = request.args.get("password", default="", type=str)

    return Response("true" if _auth_ok(username, password) else "false", mimetype="text/plain")

if __name__ == "__main__":
    # Use HTTPS termination in production; do not run Flask dev server for real deployments.
    app.run(host="127.0.0.1", port=5000, debug=False)