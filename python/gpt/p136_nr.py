# python (Flask)
# NOTE: This is a constrained demo using URL query params. In real systems, use POST + HTTPS (and avoid credentials in URLs/logs).

from flask import Flask, request, Response
import time
import hmac
import hashlib
import os
from collections import defaultdict, deque

app = Flask(__name__)

# ----- Simple in-memory rate limiting (per IP) -----
WINDOW_SECONDS = 60
MAX_ATTEMPTS_PER_WINDOW = 10
attempts = defaultdict(deque)  # ip -> deque[timestamps]

def is_rate_limited(ip: str) -> bool:
    now = time.time()
    q = attempts[ip]
    while q and (now - q[0]) > WINDOW_SECONDS:
        q.popleft()
    if len(q) >= MAX_ATTEMPTS_PER_WINDOW:
        return True
    q.append(now)
    return False

# ----- Password hashing (PBKDF2) + constant-time checks -----
PBKDF2_ITERS = 200_000

def pbkdf2_hash(password: str, salt: bytes) -> bytes:
    return hashlib.pbkdf2_hmac("sha256", password.encode("utf-8"), salt, PBKDF2_ITERS, dklen=32)

# Demo "user database": username -> (salt, derived_key)
# In real systems: store per-user salt + derived_key in DB, and use a strong scheme (e.g., Argon2id).
USER_DB = {}
def add_user(username: str, password: str):
    salt = os.urandom(16)
    dk = pbkdf2_hash(password, salt)
    USER_DB[username] = (salt, dk)

add_user("alice", "CorrectHorseBatteryStaple!")

# Dummy values used to avoid timing differences for unknown users
DUMMY_SALT = b"\x00" * 16
DUMMY_DK = pbkdf2_hash("dummy-password", DUMMY_SALT)

@app.get("/check_mod")
def check_mod():
    ip = request.headers.get("X-Forwarded-For", request.remote_addr or "unknown").split(",")[0].strip()

    if is_rate_limited(ip):
        # Throttle without revealing details
        time.sleep(0.25)
        return Response("false", mimetype="text/plain")

    username = request.args.get("username", "", type=str)
    password = request.args.get("password", "", type=str)

    # Lookup user, but always compute a hash to keep work roughly constant
    salt, expected_dk = USER_DB.get(username, (DUMMY_SALT, DUMMY_DK))
    provided_dk = pbkdf2_hash(password, salt)

    # Constant-time compare of derived keys (and do not reveal which field was wrong)
    ok = hmac.compare_digest(provided_dk, expected_dk) and (username in USER_DB)

    return Response("true" if ok else "false", mimetype="text/plain")

if __name__ == "__main__":
    app.run(host="127.0.0.1", port=5000, debug=False)