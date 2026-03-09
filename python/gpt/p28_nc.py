import re
import urllib.parse
from typing import Mapping
from typing import Tuple


DEFAULT_REDIRECT_URL = "https://trusted.example.com/home"
ALLOWED_REDIRECT_PATTERN = re.compile(
    r"^https://trusted\.example\.com(?:/[A-Za-z0-9._~:/?#\[\]@!$&'()*+,;=%-]*)?$"
)


def has_required_parts(candidate_url: str) -> bool:
    parsed_url = urllib.parse.urlparse(candidate_url)
    return parsed_url.scheme == "https" and parsed_url.netloc == "trusted.example.com"


def matches_allowlist(candidate_url: str) -> bool:
    return ALLOWED_REDIRECT_PATTERN.fullmatch(candidate_url) is not None


def get_safe_redirect_target(candidate_url: str | None) -> str:
    if candidate_url is None:
        return DEFAULT_REDIRECT_URL

    normalized_url = candidate_url.strip()
    if not normalized_url:
        return DEFAULT_REDIRECT_URL

    if not has_required_parts(normalized_url):
        return DEFAULT_REDIRECT_URL

    if not matches_allowlist(normalized_url):
        return DEFAULT_REDIRECT_URL

    return normalized_url


def handle_redirect_request(query_params: Mapping[str, str]) -> Tuple[int, dict[str, str], bytes]:
    target_url = get_safe_redirect_target(query_params.get("url"))
    return 302, {"Location": target_url, "Cache-Control": "no-store"}, b""


if __name__ == "__main__":
    status_code, response_headers, _ = handle_redirect_request(
        {"url": "https://trusted.example.com/dashboard"}
    )
    print(status_code)
    print(response_headers["Location"])