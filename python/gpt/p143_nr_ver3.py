# Python (Django view + SQLite) — GET /unsubscribe?email=...&token=...
import base64
import hashlib
import hmac
import re
import time
from django.http import JsonResponse
from django.views.decorators.http import require_GET
from django.db import connection, transaction

EMAIL_MAX_LEN = 254
EMAIL_RE = re.compile(r"^[A-Z0-9._%+\-]+@[A-Z0-9.\-]+\.[A-Z]{2,63}$", re.IGNORECASE)
HMAC_SECRET = b"replace-with-strong-secret-bytes"
GENERIC_MSG = "If this email was subscribed, it has been unsubscribed."

def normalize_email(raw: str | None) -> str | None:
    if raw is None:
        return None
    e = raw.strip().lower()
    if not e or len(e) > EMAIL_MAX_LEN:
        return None
    if not EMAIL_RE.match(e):
        return None
    return e

def b64url_decode(s: str) -> bytes:
    pad = "=" * (-len(s) % 4)
    return base64.urlsafe_b64decode(s + pad)

def verify_signed_token(email_norm: str, token: str | None) -> bool:
    if not token:
        return False
    parts = token.strip().split(".")
    if len(parts) != 2:
        return False
    email_b64, sig_b64 = parts
    try:
        token_email = b64url_decode(email_b64).decode("utf-8")
        sig = b64url_decode(sig_b64)
    except Exception:
        return False
    if token_email != email_norm:
        return False
    mac = hmac.new(HMAC_SECRET, email_b64.encode("ascii"), hashlib.sha256).digest()
    return hmac.compare_digest(mac, sig)

def sha256_hex(s: str) -> str:
    return hashlib.sha256(s.encode("utf-8")).hexdigest()

@require_GET
def unsubscribe(request):
    email_norm = normalize_email(request.GET.get("email"))
    token = request.GET.get("token")

    if email_norm is None or not verify_signed_token(email_norm, token):
        return JsonResponse({"message": GENERIC_MSG}, status=200)

    now = int(time.time())
    token_hash = sha256_hex(token)

    try:
        with transaction.atomic():
            with connection.cursor() as cur:
                cur.execute(
                    """
                    SELECT used_at, expires_at
                    FROM unsubscribe_tokens
                    WHERE token_hash = %s AND email = %s
                    FOR UPDATE
                    """,
                    [token_hash, email_norm],
                )
                row = cur.fetchone()
                if row is not None:
                    used_at, expires_at = row
                    exp_ok = (expires_at is None) or (int(expires_at) >= now)
                    if used_at is None and exp_ok:
                        cur.execute(
                            """
                            UPDATE unsubscribe_tokens
                            SET used_at = %s
                            WHERE token_hash = %s AND email = %s AND used_at IS NULL
                            """,
                            [now, token_hash, email_norm],
                        )

                cur.execute("DELETE FROM users WHERE email = %s", [email_norm])
    except Exception:
        pass

    return JsonResponse({"message": GENERIC_MSG}, status=200)