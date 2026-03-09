import hashlib
import hmac
import sqlite3
import urllib.parse
from http import HTTPStatus


DATABASE_PATH = "users.db"
DASHBOARD_PATH = "/dashboard"
LOGIN_PATH = "/login"
PBKDF2_ITERATIONS = 200_000


def validate_input(value: str | None) -> bool:
    return value is not None and 1 <= len(value.strip()) <= 128


def verify_password(password: str, stored_salt: str, stored_hash: str) -> bool:
    candidate_hash = hashlib.pbkdf2_hmac(
        "sha256",
        password.encode("utf-8"),
        stored_salt.encode("utf-8"),
        PBKDF2_ITERATIONS,
    ).hex()
    return hmac.compare_digest(candidate_hash, stored_hash)


def authenticate_user(username: str, password: str) -> bool:
    with sqlite3.connect(DATABASE_PATH) as connection:
        cursor = connection.execute(
            "SELECT password_salt, password_hash FROM users WHERE username = ?",
            (username,),
        )
        user_record = cursor.fetchone()

    if user_record is None:
        return False

    return verify_password(password, user_record[0], user_record[1])


def application(environ, start_response):
    if environ.get("REQUEST_METHOD") != "POST":
        start_response("405 Method Not Allowed", [("Content-Type", "text/plain; charset=utf-8")])
        return [b"Method Not Allowed"]

    content_length = int(environ.get("CONTENT_LENGTH") or "0")
    request_body = environ["wsgi.input"].read(content_length).decode("utf-8")
    form_data = urllib.parse.parse_qs(request_body, keep_blank_values=False)

    username = form_data.get("username", [None])[0]
    password = form_data.get("password", [None])[0]

    if validate_input(username) and validate_input(password):
        redirect_target = DASHBOARD_PATH if authenticate_user(username.strip(), password.strip()) else LOGIN_PATH
    else:
        redirect_target = LOGIN_PATH

    start_response(
        f"{HTTPStatus.FOUND.value} {HTTPStatus.FOUND.phrase}",
        [("Location", redirect_target), ("Cache-Control", "no-store")],
    )
    return [b""]