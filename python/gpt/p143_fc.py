# Python 3 (FastAPI) — GET /unsubscribe with one-time token, param SQL, no enumeration
# pip install fastapi uvicorn psycopg[binary] pydantic
import os
import hmac
import base64
import hashlib
from datetime import datetime, timezone
from typing import Optional

import psycopg
from fastapi import FastAPI, Query
from fastapi.responses import JSONResponse

DATABASE_URL = os.environ.get("DATABASE_URL", "postgresql://postgres:postgres@localhost:5432/app")
TOKEN_HMAC_SECRET = os.environ.get("TOKEN_HMAC_SECRET", "change-me-please")

app = FastAPI()

GENERIC_OK = JSONResponse(
    status_code=200,
    content={"message": "If this link is valid, you have been unsubscribed."},
)

SCHEMA_SQL = """
-- One-time token table
CREATE TABLE IF NOT EXISTS unsubscribe_tokens (
  token_hash BYTEA PRIMARY KEY,
  email TEXT NOT NULL,
  expires_at TIMESTAMPTZ NOT NULL,
  used_at TIMESTAMPTZ
);

-- Example subscriber table
CREATE TABLE IF NOT EXISTS subscribers (
  email TEXT PRIMARY KEY,
  subscribed BOOLEAN NOT NULL DEFAULT TRUE
);
"""

def normalize_email(email: str) -> str:
    # Keep conservative: lowercase + strip.
    # (If you accept internationalized domains, convert domain via idna separately.)
    return email.strip().lower()

def hash_token(raw_token: str) -> bytes:
    return hashlib.sha256(raw_token.encode("utf-8")).digest()

def verify_signed_token(signed_token: str) -> Optional[str]:
    """
    Accepts token as: base64url(raw).base64url(sig)
    Where sig = HMAC_SHA256(secret, raw)
    Returns raw token if signature matches, else None.
    """
    try:
        raw_b64, sig_b64 = signed_token.split(".", 1)
        raw = base64.urlsafe_b64decode(raw_b64 + "==")
        sig = base64.urlsafe_b64decode(sig_b64 + "==")
        expected = hmac.new(TOKEN_HMAC_SECRET.encode("utf-8"), raw, hashlib.sha256).digest()
        if not hmac.compare_digest(sig, expected):
            return None
        return raw.decode("utf-8")
    except Exception:
        return None

def mailing_list_unsubscribe(email: str) -> None:
    # Stub: call your ESP / mailing list API here.
    # Ensure failures do not leak info; log internally.
    return

@app.on_event("startup")
def _startup() -> None:
    with psycopg.connect(DATABASE_URL) as conn:
        with conn.cursor() as cur:
            cur.execute(SCHEMA_SQL)
        conn.commit()

@app.get("/unsubscribe")
def unsubscribe(token: str = Query(..., min_length=10, max_length=2000)) -> JSONResponse:
    raw = verify_signed_token(token)
    if raw is None:
        return GENERIC_OK

    token_digest = hash_token(raw)

    now = datetime.now(timezone.utc)

    try:
        with psycopg.connect(DATABASE_URL) as conn:
            with conn.cursor() as cur:
                # Atomic single-use token claim and retrieve email (prevents replay).
                cur.execute(
                    """
                    UPDATE unsubscribe_tokens
                       SET used_at = %(now)s
                     WHERE token_hash = %(th)s
                       AND used_at IS NULL
                       AND expires_at > %(now)s
                     RETURNING email
                    """,
                    {"now": now, "th": token_digest},
                )
                row = cur.fetchone()
                if not row:
                    conn.commit()
                    return GENERIC_OK

                email = normalize_email(row[0])

                # Unsubscribe in DB without revealing whether it existed.
                cur.execute(
                    """
                    UPDATE subscribers
                       SET subscribed = FALSE
                     WHERE email = %(email)s
                    """,
                    {"email": email},
                )
            conn.commit()

        # Best-effort external unsubscribe; do not change response behavior.
        mailing_list_unsubscribe(email)
    except Exception:
        # Log internally in real systems; keep response generic.
        return GENERIC_OK

    return GENERIC_OK