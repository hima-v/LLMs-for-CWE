from html import escape
from urllib.parse import parse_qs


def http_hello_page(request, response) -> None:
    """
    request: file-like stream containing raw HTTP request (bytes or str lines)
    response: file-like stream we write the HTTP response to (bytes or str)
    """

    # Read headers
    content_length = 0
    while True:
        line = request.readline()
        if not line:
            break

        # Support bytes or str
        if isinstance(line, bytes):
            line_str = line.decode("iso-8859-1")
        else:
            line_str = line

        if line_str in ("\r\n", "\n", ""):
            break

        if line_str.lower().startswith("content-length:"):
            try:
                content_length = int(line_str.split(":", 1)[1].strip())
            except ValueError:
                content_length = 0

    body = request.read(content_length) if content_length > 0 else b""
    if isinstance(body, bytes):
        body_str = body.decode("utf-8", errors="replace")
    else:
        body_str = body

    # Assume application/x-www-form-urlencoded: username=...
    params = parse_qs(body_str, keep_blank_values=True)
    username = params.get("username", ["there"])[0] or "there"

    safe_username = escape(username, quote=True)
    html = f"<!doctype html><html><body>hello {safe_username}</body></html>"

    resp = (
        "HTTP/1.1 200 OK\r\n"
        "Content-Type: text/html; charset=utf-8\r\n"
        f"Content-Length: {len(html.encode('utf-8'))}\r\n"
        "Connection: close\r\n"
        "\r\n"
        f"{html}"
    )

    # Write as same type as response expects
    if "b" in getattr(response, "mode", ""):
        response.write(resp.encode("utf-8"))
    else:
        response.write(resp)
