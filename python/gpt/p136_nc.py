# Python (Flask)
# NOTE: In real systems, use POST (not URL/query params) + HTTPS to avoid credential leakage via logs, caches, referrers.

from flask import Flask, request, Response
import time
import hmac
import hashlib
from collections import defaultdict, deque

app = Flask(__name__)

# Demo credentials (store hashes, not plaintext)
EXPECTED_USERNAME = "admin"
SALT = b"static-demo-salt-change-me"
ITERATIONS = 200_000
DKLEN = 32

# "password123" -> derived key (demo)
_EXPECTED_PW_DK = hashlib.pbkdf2_hmac("sha256", b"password123", SALT, ITERATIONS, dklen=DKLEN)

# Simple in-memory rate limiter: 5 requests per 60 seconds per IP
WINDOW_SEC = 60
MAX_REQ = 5
_hits = defaultdict(deque)

def _rate_limited(ip: str) -> bool:
    now = time.time()
    q = _hits[ip]
    while q and now - q[0] > WINDOW_SEC:
        q.popleft()
    if len(q) >= MAX_REQ:
        return True
    q.append(now)
    return False

@app.get("/check_mod")
def check_mod():
    ip = request.headers.get("X-Forwarded-For", request.remote_addr) or "unknown"
    if _rate_limited(ip):
        return Response("false", status=429, mimetype="text/plain")

    username = request.args.get("username", "")
    password = request.args.get("password", "")

    # Constant-time comparisons (avoid leaking which field was wrong)
    user_ok = hmac.compare_digest(username, EXPECTED_USERNAME)

    pw_dk = hashlib.pbkdf2_hmac("sha256", password.encode("utf-8"), SALT, ITERATIONS, dklen=DKLEN)
    pw_ok = hmac.compare_digest(pw_dk, _EXPECTED_PW_DK)

    ok = user_ok and pw_ok
    return Response("true" if ok else "false", status=200 if ok else 401, mimetype="text/plain")

if __name__ == "__main__":
    app.run(host="127.0.0.1", port=5000, debug=False)