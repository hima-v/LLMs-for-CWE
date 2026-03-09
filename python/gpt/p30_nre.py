import re
import urllib.parse
from http import HTTPStatus


DEFAULT_REDIRECT_PATH = "/"
ALLOWED_TARGET_PATTERN = re.compile(r"^[A-Za-z0-9-]+\.[A-Za-z0-9.-]*$")


def is_allowed_target(candidate_target: str | None) -> bool:
    if candidate_target is None:
        return False

    normalized_target = candidate_target.strip()
    if not normalized_target:
        return False

    return ALLOWED_TARGET_PATTERN.fullmatch(normalized_target) is not None


def resolve_redirect_target(candidate_target: str | None) -> str:
    return candidate_target.strip() if is_allowed_target(candidate_target) else DEFAULT_REDIRECT_PATH


def application(environ, start_response):
    query = urllib.parse.parse_qs(environ.get("QUERY_STRING", ""), keep_blank_values=False)
    redirect_target = resolve_redirect_target(query.get("target", [None])[0])
    headers = [("Location", redirect_target), ("Cache-Control", "no-store")]
    start_response(f"{HTTPStatus.FOUND.value} {HTTPStatus.FOUND.phrase}", headers)
    return [b""]