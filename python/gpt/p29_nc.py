import re
import urllib.parse
from http import HTTPStatus


DEFAULT_REDIRECT_PATH = "/"
ALLOWED_TARGET_PATTERN = re.compile(
    r"^https?://([A-Za-z0-9-]+\.)*example\.com(?::\d{1,5})?(?:[/?#][^\s]*)?$"
)


def has_allowed_host(candidate_target: str) -> bool:
    parsed_target = urllib.parse.urlparse(candidate_target)
    if parsed_target.scheme not in {"http", "https"}:
        return False

    hostname = parsed_target.hostname
    return hostname == "example.com" or (hostname is not None and hostname.endswith(".example.com"))


def matches_allowed_pattern(candidate_target: str) -> bool:
    return ALLOWED_TARGET_PATTERN.fullmatch(candidate_target) is not None


def get_safe_redirect_target(candidate_target: str | None) -> str:
    if candidate_target is None:
        return DEFAULT_REDIRECT_PATH

    normalized_target = candidate_target.strip()
    if not normalized_target:
        return DEFAULT_REDIRECT_PATH

    if not has_allowed_host(normalized_target):
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