# python (FastAPI) - secure unsubscribe via signed token (GET)
from fastapi import FastAPI, Request, HTTPException
from pydantic import BaseModel
import re
import hmac
import hashlib
import base64
import json
import time
import sqlite3

app = FastAPI()

DB_PATH = "app.db"
UNSUB_SECRET = b"CHANGE_ME_TO_A_LONG_RANDOM_SECRET"
GENERIC_MSG = {"message": "If the address is subscribed, it has been unsubscribed."}

EMAIL_RE = re.compile(r"^[A-Z0-9._%+\-]+@[A-Z0-9.\-]+\.[A-Z]{2,}$", re.IGNORECASE)

def normalize_email(email: str) -> str:
    email = (email or "").strip()
    if not email or len(email) > 254:
        raise ValueError("invalid")
    email = email.casefold()
    if not EMAIL_RE.match(email):
        raise ValueError("invalid")
    return email

def b64url_encode(b: bytes) -> str:
    return base64.urlsafe_b64encode(b).rstrip(b"=").decode("ascii")

def b64url_decode(s: str) -> bytes:
    s = (s or "").encode("ascii")
    pad = b"=" * ((4 - (len(s) % 4)) % 4)
    return base64.urlsafe_b64decode(s + pad)

def sign(payload: bytes) -> str:
    mac = hmac.new(UNSUB_SECRET, payload, hashlib.sha256).digest()
    return b64url_encode(mac)

def verify_signed_token(token: str, max_age_seconds: int = 7 * 24 * 3600) -> dict:
    try:
        raw = b64url_decode(token)
        obj = json.loads(raw.decode("utf-8"))
        email = normalize_email(obj.get("email", ""))
        exp = int(obj.get("exp", 0))
        sig = str(obj.get("sig", ""))
        now = int(time.time())
        if exp < now or exp > now + max_age_seconds:
            raise ValueError("expired")
        unsigned = json.dumps({"email": email, "exp": exp}, separators=(",", ":"), sort_keys=True).encode("utf-8")
        expected = sign(unsigned)
        if not hmac.compare_digest(expected, sig):
            raise ValueError("bad sig")
        return {"email": email, "exp": exp}
    except Exception:
        raise ValueError("invalid token")

def db_conn():
    conn = sqlite3.connect(DB_PATH)
    conn.execute("PRAGMA foreign_keys = ON")
    return conn

def mailing_list_unsubscribe(email: str) -> None:
    # Placeholder for ESP integration; keep idempotent.
    return

@app.get("/unsubscribe")
def unsubscribe(t: str, request: Request):
    # Generic response prevents enumeration; token required (GET-safe).
    try:
        data = verify_signed_token(t)
        email = data["email"]
    except Exception:
        return GENERIC_MSG

    try:
        with db_conn() as conn:
            cur = conn.cursor()
            cur.execute("SELECT 1 FROM users WHERE email = ?", (email,))
            exists = cur.fetchone() is not None
            if exists:
                cur.execute("DELETE FROM users WHERE email = ?", (email,))
        if exists:
            mailing_list_unsubscribe(email)
    except Exception:
        # Avoid leaking operational details
        return GENERIC_MSG

    return GENERIC_MSG

# helper: create a token (server-side usage)
def make_unsub_token(email: str, ttl_seconds: int = 7 * 24 * 3600) -> str:
    email = normalize_email(email)
    exp = int(time.time()) + ttl_seconds
    unsigned = json.dumps({"email": email, "exp": exp}, separators=(",", ":"), sort_keys=True).encode("utf-8")
    sig = sign(unsigned)
    token_obj = {"email": email, "exp": exp, "sig": sig}
    return b64url_encode(json.dumps(token_obj, separators=(",", ":"), sort_keys=True).encode("utf-8"))