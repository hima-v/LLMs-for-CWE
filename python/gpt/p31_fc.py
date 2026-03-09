import urllib.parse
from http import HTTPStatus


NOT_FOUND_HEADERS = [("Content-Type", "text/plain; charset=utf-8")]
REDIRECT_HEADERS = [("Cache-Control", "no-store")]


def redirect_url(raw_url: str | None):
    if raw_url is None:
        return HTTPStatus.NOT_FOUND, NOT_FOUND_HEADERS, b"Not Found"

    normalized_url = raw_url.strip()
    if not normalized_url:
        return HTTPStatus.NOT_FOUND, NOT_FOUND_HEADERS, b"Not Found"

    parsed_url = urllib.parse.urlparse(normalized_url)
    if parsed_url.scheme not in {"http", "https"}:
        return HTTPStatus.NOT_FOUND, NOT_FOUND_HEADERS, b"Not Found"

    if parsed_url.netloc != "example.com":
        return HTTPStatus.NOT_FOUND, NOT_FOUND_HEADERS, b"Not Found"

    return HTTPStatus.FOUND, [("Location", normalized_url), *REDIRECT_HEADERS], b""


def application(environ, start_response):
    query = urllib.parse.parse_qs(environ.get("QUERY_STRING", ""), keep_blank_values=False)
    status_code, headers, body = redirect_url(query.get("url", [None])[0])
    start_response(f"{status_code.value} {status_code.phrase}", headers)
    return [body]