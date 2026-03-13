# Python (Flask)
# NOTE: Demo uses URL query params; in real systems use POST + HTTPS (avoid secrets in URLs/logs).

from flask import Flask, request, Response
import time
import hmac
import hashlib

app = Flask(__name__)

# Demo credential store: username -> salt + PBKDF2 hash
_USERS = {
    "alice": {
        "salt": b"demo_salt_alice",
        "dk": hashlib.pbkdf2_hmac("sha256", b"correcthorsebatterystaple", b"demo_salt_alice", 200_000),
    },
}

# Dummy values to equalize work when user is missing (reduces user-enumeration timing leaks)
_DUMMY_SALT = b"demo_salt_dummy____"
_DUMMY_DK = hashlib.pbkdf2_hmac("sha256", b"dummy_password", _DUMMY_SALT, 200_000)

# Basic per-IP throttling
_WINDOW_SECONDS = 60
_MAX_ATTEMPTS = 10
_attempts = {}  # ip -> list[timestamps]

def _allow(ip: str) -> bool:
    now = time.time()
    ts = _attempts.get(ip, [])
    ts = [t for t in ts if now - t < _WINDOW_SECONDS]
    if len(ts) >= _MAX_ATTEMPTS:
        _attempts[ip] = ts
        return False
    ts.append(now)
    _attempts[ip] = ts
    return True

@app.get("/check_mod")
def check_mod():
    ip = (request.headers.get("X-Forwarded-For") or request.remote_addr or "unknown").split(",")[0].strip()
    if not _allow(ip):
        return Response("false", mimetype="text/plain")

    username = request.args.get("username", "", type=str)
    password = request.args.get("password", "", type=str)

    rec = _USERS.get(username)
    salt = rec["salt"] if rec else _DUMMY_SALT
    expected = rec["dk"] if rec else _DUMMY_DK

    provided = hashlib.pbkdf2_hmac("sha256", password.encode("utf-8"), salt, 200_000)
    ok = hmac.compare_digest(provided, expected)

    return Response("true" if ok else "false", mimetype="text/plain")

if __name__ == "__main__":
    # For demo only. Put behind HTTPS in production.
    app.run(host="0.0.0.0", port=5000, debug=False)