import hashlib
import hmac
import secrets
import urllib.parse
from http import HTTPStatus


LOGIN_PATH = "/login"
INDEX_PATH = "/"
USER_PATH = "/user"
ADMIN_PATH = "/admin"
PBKDF2_ITERATIONS = 200_000
USERS = {
    "user": {"role": "user", "salt": "user-salt", "hash": hashlib.pbkdf2_hmac("sha256", b"user-password", b"user-salt", PBKDF2_ITERATIONS).hex()},
    "admin": {"role": "admin", "salt": "admin-salt", "hash": hashlib.pbkdf2_hmac("sha256", b"admin-password", b"admin-salt", PBKDF2_ITERATIONS).hex()},
}
SESSIONS: dict[str, dict[str, str]] = {}


def validate_input(value: str | None) -> bool:
    return value is not None and 1 <= len(value.strip()) <= 128


def verify_password(username: str, password: str) -> bool:
    user = USERS.get(username)
    if user is None:
        return False
    candidate_hash = hashlib.pbkdf2_hmac("sha256", password.encode("utf-8"), user["salt"].encode("utf-8"), PBKDF2_ITERATIONS).hex()
    return hmac.compare_digest(candidate_hash, user["hash"])


def parse_cookies(environ) -> dict[str, str]:
    cookie_header = environ.get("HTTP_COOKIE", "")
    cookies = {}
    for part in cookie_header.split(";"):
        if "=" in part:
            key, value = part.strip().split("=", 1)
            cookies[key] = value
    return cookies


def get_session(environ):
    session_id = parse_cookies(environ).get("session_id")
    return SESSIONS.get(session_id)


def respond(start_response, status: HTTPStatus, body: bytes, headers=None):
    all_headers = [("Content-Type", "text/plain; charset=utf-8"), ("Cache-Control", "no-store")]
    if headers:
        all_headers.extend(headers)
    start_response(f"{status.value} {status.phrase}", all_headers)
    return [body]


def login(environ, start_response):
    return respond(start_response, HTTPStatus.OK, b"Login page")


def do_login(environ, start_response):
    content_length = int(environ.get("CONTENT_LENGTH") or "0")
    request_body = environ["wsgi.input"].read(content_length).decode("utf-8")
    form_data = urllib.parse.parse_qs(request_body, keep_blank_values=False)
    username = form_data.get("username", [None])[0]
    password = form_data.get("password", [None])[0]

    if not validate_input(username) or not validate_input(password):
        return respond(start_response, HTTPStatus.BAD_REQUEST, b"Invalid credentials")

    normalized_username = username.strip()
    normalized_password = password.strip()
    if not verify_password(normalized_username, normalized_password):
        return respond(start_response, HTTPStatus.UNAUTHORIZED, b"Authentication failed")

    session_id = secrets.token_urlsafe(32)
    SESSIONS[session_id] = {"username": normalized_username, "role": USERS[normalized_username]["role"]}
    return respond(start_response, HTTPStatus.FOUND, b"", [("Location", INDEX_PATH), ("Set-Cookie", f"session_id={session_id}; HttpOnly; SameSite=Strict")])


def index(environ, start_response):
    session = get_session(environ)
    if session is None:
        return respond(start_response, HTTPStatus.FOUND, b"", [("Location", LOGIN_PATH)])
    return respond(start_response, HTTPStatus.OK, f"Welcome {session['username']}".encode("utf-8"))


def user_page(environ, start_response):
    session = get_session(environ)
    if session is None:
        return respond(start_response, HTTPStatus.FOUND, b"", [("Location", LOGIN_PATH)])
    return respond(start_response, HTTPStatus.OK, b"User page")


def admin_page(environ, start_response):
    session = get_session(environ)
    if session is None:
        return respond(start_response, HTTPStatus.FOUND, b"", [("Location", LOGIN_PATH)])
    if session.get("role") != "admin":
        return respond(start_response, HTTPStatus.FORBIDDEN, b"Forbidden")
    return respond(start_response, HTTPStatus.OK, b"Admin page")


def application(environ, start_response):
    route = environ.get("PATH_INFO", INDEX_PATH)
    method = environ.get("REQUEST_METHOD", "GET")
    if route == LOGIN_PATH and method == "GET":
        return login(environ, start_response)
    if route == "/do_login" and method == "POST":
        return do_login(environ, start_response)
    if route == INDEX_PATH and method == "GET":
        return index(environ, start_response)
    if route == USER_PATH and method == "GET":
        return user_page(environ, start_response)
    if route == ADMIN_PATH and method == "GET":
        return admin_page(environ, start_response)
    return respond(start_response, HTTPStatus.NOT_FOUND, b"Not found")