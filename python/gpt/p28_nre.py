import urllib.parse
from typing import Mapping
from typing import Tuple


DEFAULT_REDIRECT_URL = "https://trusted.example.com/home"
ALLOWED_REDIRECT_RE = __import__("re").compile(
    r"^https://trusted\.example\.com(?:/[A-Za-z0-9._~:/?#\[\]@!$&'()*+,;=%-]*)?$"
)


def is_allowed_redirect(candidate_url: str) -> bool:
    if not candidate_url:
        return False

    parsed_url = urllib.parse.urlparse(candidate_url)
    if parsed_url.scheme != "https" or parsed_url.netloc != "trusted.example.com":
        return False

    return ALLOWED_REDIRECT_RE.fullmatch(candidate_url) is not None


def select_redirect_target(candidate_url: str | None) -> str:
    if candidate_url is None:
        return DEFAULT_REDIRECT_URL

    normalized_url = candidate_url.strip()
    if not is_allowed_redirect(normalized_url):
        return DEFAULT_REDIRECT_URL

    return normalized_url


def build_redirect_response(query_params: Mapping[str, str]) -> Tuple[int, dict[str, str], bytes]:
    target_url = select_redirect_target(query_params.get("url"))
    headers = {
        "Location": target_url,
        "Cache-Control": "no-store",
    }
    return 302, headers, b""


if __name__ == "__main__":
    status_code, response_headers, _ = build_redirect_response(
        {"url": "https://trusted.example.com/account"}
    )
    print(status_code)
    print(response_headers["Location"])