# Python (file-like request/response objects)
import re

MAX_USERNAME_LEN = 64
_ALLOWED_RE = re.compile(r"^[A-Za-z0-9_.-]{1,64}$")

def _write_response(response, status: int, reason: str, body: str) -> None:
    body_bytes = body.encode("utf-8", errors="replace")
    headers = (
        f"HTTP/1.1 {status} {reason}\r\n"
        "Content-Type: text/plain; charset=utf-8\r\n"
        "X-Content-Type-Options: nosniff\r\n"
        "Connection: close\r\n"
        f"Content-Length: {len(body_bytes)}\r\n"
        "\r\n"
    ).encode("ascii", errors="strict")
    response.write(headers)
    response.write(body_bytes)

def _extract_username(raw: str) -> str | None:
    m = re.search(r"(?:\b|&)username=([^&\s\r\n]+)", raw)
    return m.group(1) if m else None

def _validate_username(u: str) -> bool:
    return bool(_ALLOWED_RE.fullmatch(u or ""))

def http_hello_page(request, response) -> None:
    try:
        raw_bytes = request.read()
        if isinstance(raw_bytes, str):
            raw = raw_bytes
        else:
            raw = raw_bytes.decode("utf-8", errors="replace")
    except Exception:
        _write_response(response, 400, "Bad Request", "Bad Request\n")
        return

    username = _extract_username(raw)
    if username is None:
        _write_response(response, 400, "Bad Request", "Bad Request\n")
        return

    if not _validate_username(username):
        _write_response(response, 400, "Bad Request", "Invalid username\n")
        return

    _write_response(response, 200, "OK", f"Hello {username}\n")