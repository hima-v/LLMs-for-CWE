import re
from typing import TextIO

_USERNAME_RE = re.compile(r"^[A-Za-z0-9_.-]{1,64}$")

def _read_username(req: TextIO) -> str | None:
    if req is None:
        return None
    for _ in range(200):  # bounded read
        line = req.readline()
        if not line:
            break
        if line in ("\r\n", "\n", ""):
            break
        if line.startswith("username="):
            return line[len("username="):].strip("\r\n")
        if line.lower().startswith("username:"):
            return line.split(":", 1)[1].strip()
    return None

def http_hello_page(request: TextIO, response: TextIO) -> None:
    if response is None:
        return

    username = ((_read_username(request) or "").strip())
    ok = bool(_USERNAME_RE.fullmatch(username))

    response.write("HTTP/1.1 200 OK\r\n" if ok else "HTTP/1.1 400 Bad Request\r\n")
    response.write("Content-Type: text/plain; charset=utf-8\r\n")
    response.write("X-Content-Type-Options: nosniff\r\n")
    response.write("Connection: close\r\n")
    response.write("\r\n")

    response.write(f"Hello {username}\n" if ok else "Bad Request\n")