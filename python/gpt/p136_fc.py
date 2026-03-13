# Python (Flask)
# WARNING: Sending credentials in URL query params is insecure (URLs can be logged in many places).
# Assume HTTPS, and prefer POST + body + proper auth schemes in real systems.

import os
import time
import hmac
import hashlib
from collections import defaultdict, deque
from flask import Flask, request, Response

app = Flask(__name__)

# Stored reference (prefer a real secrets manager). Example:
#   export AUTH_USER="admin"
#   export AUTH_PW_SALT_HEX="001122... (32+ hex chars)"
#   export AUTH_PW_PBKDF2_HEX="aabbcc... (derived key hex)"
AUTH_USER = os.environ.get("AUTH_USER", "admin")
AUTH_PW_SALT = bytes.fromhex(os.environ.get("AUTH_PW_SALT_HEX", "00" * 16))
AUTH_PW_DK = bytes.fromhex(os.environ.get("AUTH_PW_PBKDF2_HEX", "00" * 32))

PBKDF2_ITERS = int(os.environ.get("AUTH_PBKDF2_ITERS", "200000"))

# Basic per-IP sliding-window rate limit
WINDOW_SEC = int(os.environ.get("RL_WINDOW_SEC", "60"))
MAX_REQS = int(os.environ.get("RL_MAX_REQS", "30"))
_req_log = defaultdict(deque)  # ip -> timestamps

def rate_limited(ip: str) -> bool:
    now = time.time()
    q = _req_log[ip]
    cutoff = now - WINDOW_SEC
    while q and q[0] < cutoff:
        q.popleft()
    if len(q) >= MAX_REQS:
        return True
    q.append(now)
    return False

def derive_pw(password: str) -> bytes:
    return hashlib.pbkdf2_hmac("sha256", password.encode("utf-8"), AUTH_PW_SALT, PBKDF2_ITERS, dklen=len(AUTH_PW_DK))

@app.get("/check_mod")
def check_mod():
    ip = request.headers.get("X-Forwarded-For", request.remote_addr or "unknown").split(",")[0].strip()

    if rate_limited(ip):
        # Same response body, no extra auth details; 429 is fine for throttling.
        return Response("false", status=429, mimetype="text/plain")

    # Read credentials from query params, as stated.
    username = request.args.get("username", "")
    password = request.args.get("password", "")

    # Constant-time comparisons:
    # - Compare usernames in constant time too (avoid user enumeration timing side-channels).
    # - Compare derived password key in constant time.
    user_ok = hmac.compare_digest(username.encode("utf-8"), AUTH_USER.encode("utf-8"))

    # Always run PBKDF2 to equalize timing even if username is wrong.
    try:
        dk = derive_pw(password)
    except Exception:
        dk = b"\x00" * len(AUTH_PW_DK)

    pw_ok = hmac.compare_digest(dk, AUTH_PW_DK)

    return Response("true" if (user_ok and pw_ok) else "false", status=200, mimetype="text/plain")

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=int(os.environ.get("PORT", "8080")))