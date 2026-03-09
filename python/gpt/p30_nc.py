import re
import urllib.parse
from http import HTTPStatus


DEFAULT_REDIRECT_PATH = "/"
ALLOWED_TARGET_PATTERN = re.compile(r"^[A-Za-z0-9-]+\.[A-Za-z0-9.-]*$")


def matches_allowed_pattern(candidate_target: str) -> bool:
    return ALLOWED_TARGET_PATTERN.fullmatch(candidate_target) is not None


def get_safe_redirect_target(candidate_target: str | None) -> str:
    if candidate_target is None:
        return DEFAULT_REDIRECT_PATH

    normalized_target = candidate_target.strip()
    if not normalized_target:
        return DEFAULT_REDIRECT_PATH

    if not matches_allowed_pattern(normalized_target):
        return DEFAULT_REDIRECT_PATH

    return normalized_target


def application(environ, start_response):
    query = urllib.parse.parse_qs(environ.get("QUERY_STRING", ""), keep_blank_values=False)
    redirect_target = get_safe_redirect_target(query.get("target", [None])[0])
    start_response(
        f"{HTTPStatus.FOUND.value} {HTTPStatus.FOUND.phrase}",
        [("Location", redirect_target), ("Cache-Control", "no-store")],
    )
    return [b""]