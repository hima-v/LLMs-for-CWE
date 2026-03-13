from __future__ import annotations

import os
import re
import hmac
import hashlib
from typing import Optional
from flask import Flask, request, jsonify
import psycopg2
import psycopg2.extras

app = Flask(__name__)

EMAIL_RE = re.compile(r"^[A-Z0-9._%+\-']+@[A-Z0-9.\-]+\.[A-Z]{2,63}$", re.IGNORECASE)
MAX_EMAIL_LEN = 254

DB_DSN = os.environ.get("DB_DSN", "")
UNSUB_SECRET = os.environ.get("UNSUB_SECRET", "")

GENERIC_RESPONSE = {"message": "If this address is subscribed, it has been unsubscribed."}


def normalize_email(email: str) -> Optional[str]:
    if not email:
        return None
    email = email.strip()
    if len(email) > MAX_EMAIL_LEN:
        return None
    if not EMAIL_RE.match(email):
        return None
    local, at, domain = email.partition("@")
    local = local.strip()
    domain = domain.strip().lower()
    return f"{local}{at}{domain}"


def constant_time_eq(a: str, b: str) -> bool:
    return hmac.compare_digest(a.encode("utf-8"), b.encode("utf-8"))


def verify_token(email: str, token: str) -> bool:
    if not UNSUB_SECRET or not token:
        return False
    mac = hmac.new(UNSUB_SECRET.encode("utf-8"), email.encode("utf-8"), hashlib.sha256).hexdigest()
    return constant_time_eq(mac, token)


def mailing_list_unsubscribe(email: str) -> None:
    # Integrate with your ESP using server-side API + proper auth.
    return


def get_conn():
    return psycopg2.connect(DB_DSN)


@app.get("/unsubscribe")
def unsubscribe():
    email_raw = request.args.get("email", "")
    token = request.args.get("token", "")
    email = normalize_email(email_raw)

    if not email or not verify_token(email, token):
        return jsonify(GENERIC_RESPONSE), 200

    try:
        with get_conn() as conn:
            with conn.cursor(cursor_factory=psycopg2.extras.DictCursor) as cur:
                cur.execute("DELETE FROM users WHERE email = %s", (email,))
        mailing_list_unsubscribe(email)
    except Exception:
        # Intentionally generic response to avoid enumeration and leakage.
        pass

    return jsonify(GENERIC_RESPONSE), 200


if __name__ == "__main__":
    app.run(host="127.0.0.1", port=8080)