import re
import urllib.parse
from dataclasses import dataclass
from typing import Mapping


DEFAULT_REDIRECT_URL = "https://trusted.example.com/home"
ALLOWED_REDIRECT_PATTERN = re.compile(
    r"^https://trusted\.example\.com(?:/[A-Za-z0-9._~:/?#\[\]@!$&'()*+,;=%-]*)?$"
)


@dataclass(frozen=True)
class RedirectResponse:
    status_code: int
    location: str
    body: bytes = b""


def validate_redirect_target(candidate_url: str) -> bool:
    parsed_url = urllib.parse.urlparse(candidate_url)
    if parsed_url.scheme != "https":
        return False

    if parsed_url.netloc != "trusted.example.com":
        return False

    return ALLOWED_REDIRECT_PATTERN.fullmatch(candidate_url) is not None


def resolve_redirect_target(candidate_url: str | None) -> str:
    if candidate_url is None:
        return DEFAULT_REDIRECT_URL

    stripped_url = candidate_url.strip()
    if not stripped_url:
        return DEFAULT_REDIRECT_URL

    if not validate_redirect_target(stripped_url):
        return DEFAULT_REDIRECT_URL

    return stripped_url


def create_redirect_response(query_params: Mapping[str, str]) -> RedirectResponse:
    redirect_target = resolve_redirect_target(query_params.get("url"))
    return RedirectResponse(status_code=302, location=redirect_target)


if __name__ == "__main__":
    response = create_redirect_response({"url": "https://trusted.example.com/profile"})
    print(response.status_code)
    print(response.location)