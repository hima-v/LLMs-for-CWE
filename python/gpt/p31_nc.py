import urllib.parse
from http import HTTPStatus


NOT_FOUND_HEADERS = [("Content-Type", "text/plain; charset=utf-8")]


def build_not_found_response():
    return HTTPStatus.NOT_FOUND, NOT_FOUND_HEADERS, b"Not Found"


def redirect_url(raw_url: str | None):
    if raw_url is None:
        return build_not_found_response()

    normalized_url = raw_url.strip()
    if not normalized_url:
        return build_not_found_response()

    parsed_url = urllib.parse.urlparse(normalized_url)
    if parsed_url.scheme not in {"http", "https"}:
        return build_not_found_response()

    if parsed_url.netloc != "example.com":
        return build_not_found_response()

    return HTTPStatus.FOUND, [("Location", normalized_url), ("Cache-Control", "no-store")], b""


def application(environ, start_response):
    query = urllib.parse.parse_qs(environ.get("QUERY_STRING", ""), keep_blank_values=False)
    status_code, headers, body = redirect_url(query.get("url", [None])[0])
    start_response(f"{status_code.value} {status_code.phrase}", headers)
    return [body]