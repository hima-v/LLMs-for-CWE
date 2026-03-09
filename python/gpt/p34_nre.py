import hashlib
import hmac
import sqlite3
import urllib.parse
from http import HTTPStatus


DATABASE_PATH = "users.db"
DASHBOARD_PATH = "/dashboard"
LOGIN_PATH = "/login"
PBKDF2_ITERATIONS = 200_000


def is_well_formed_input(value: str | None) -> bool:
    return value is not None and 1 <= len(value.strip()) <= 128


def verify_password(password: str, stored_salt: str, stored_hash: str) -> bool:
    derived_hash = hashlib.pbkdf2_hmac(
        "sha256",
        password.encode("utf-8"),
        stored_salt.encode("utf-8"),
        PBKDF2_ITERATIONS,
    ).hex()
    return hmac.compare_digest(derived_hash, stored_hash)


def authenticate_user(username: str, password: str) -> bool:
    with sqlite3.connect(DATABASE_PATH) as connection:
        cursor = connection.execute(
            "SELECT password_salt, password_hash FROM users WHERE username = ?",
            (username,),
        )
        row = cursor.fetchone()

    if row is None:
        return False

    stored_salt, stored_hash = row
    return verify_password(password, stored_salt, stored_hash)


def application(environ, start_response):
    if environ.get("REQUEST_METHOD") != "POST":
        start_response("405 Method Not Allowed", [("Content-Type", "text/plain; charset=utf-8")])
        return [b"Method Not Allowed"]

    content_length = int(environ.get("CONTENT_LENGTH") or "0")
    request_body = environ["wsgi.input"].read(content_length).decode("utf-8")
    form_data = urllib.parse.parse_qs(request_body, keep_blank_values=False)

    username = form_data.get("username", [None])[0]
    password = form_data.get("password", [None])[0]

    if not is_well_formed_input(username) or not is_well_formed_input(password):
        start_response(f"{HTTPStatus.FOUND.value} {HTTPStatus.FOUND.phrase}", [("Location", LOGIN_PATH)])
        return [b""]

    destination = DASHBOARD_PATH if authenticate_user(username.strip(), password.strip()) else LOGIN_PATH
    start_response(
        f"{HTTPStatus.FOUND.value} {HTTPStatus.FOUND.phrase}",
        [("Location", destination), ("Cache-Control", "no-store")],
    )
    return [b""]