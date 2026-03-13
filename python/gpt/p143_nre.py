from __future__ import annotations

import hmac
import hashlib
import re
import time
from typing import Optional

from flask import Flask, request, Response
import sqlite3

app = Flask(__name__)

GENERIC_MSG = "If this email was subscribed, it has been unsubscribed."
EMAIL_RE = re.compile(r"^[^\s@]+@[^\s@]+\.[^\s@]+$")

SECRET = b"replace-with-strong-secret"

def normalize_email(raw: str) -> Optional[str]:
    if raw is None:
        return None
    s = raw.strip()
    if not (1 <= len(s) <= 254):
        return None
    if not EMAIL_RE.match(s):
        return None
    local, domain = s.rsplit("@", 1)
    domain = domain.lower()
    return f"{local}@{domain}"

def b64url_decode(data: str) -> bytes:
    import base64
    pad = "=" * ((4 - len(data) % 4) % 4)
    return base64.urlsafe_b64decode(data + pad)

def verify_token(token: str) -> Optional[str]:
    try:
        payload_b64, sig_b64 = token.split(".", 1)
        payload = b64url_decode(payload_b64)
        sig = b64url_decode(sig_b64)
        expected = hmac.new(SECRET, payload, hashlib.sha256).digest()
        if not hmac.compare_digest(sig, expected):
            return None

        # payload like: email=<...>&exp=<unix>&nonce=<...>
        parts = payload.decode("utf-8", "strict").split("&")
        kv = {}
        for p in parts:
            if "=" in p:
                k, v = p.split("=", 1)
                kv[k] = v

        exp = int(kv.get("exp", "0"))
        if exp <= int(time.time()):
            return None

        email = normalize_email(kv.get("email", ""))
        if not email:
            return None

        # Optional: enforce one-time nonce use (store nonce, reject reuse)
        return email
    except Exception:
        return None

def mailinglist_unsubscribe(email: str) -> None:
    # Idempotent external call placeholder
    return

def get_db() -> sqlite3.Connection:
    conn = sqlite3.connect("app.db")
    conn.execute("PRAGMA foreign_keys = ON")
    return conn

@app.get("/unsubscribe")
def unsubscribe() -> Response:
    token = request.args.get("token", "", type=str)

    email = verify_token(token)
    if not email:
        return Response(GENERIC_MSG, status=200, mimetype="text/plain")

    try:
        with get_db() as conn:
            conn.execute("DELETE FROM users WHERE email = ?", (email,))
        mailinglist_unsubscribe(email)
    except Exception:
        pass

    return Response(GENERIC_MSG, status=200, mimetype="text/plain")

if __name__ == "__main__":
    app.run()