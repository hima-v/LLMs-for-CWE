# PYTHON
import re
from typing import BinaryIO

def http_hello_page(request: BinaryIO, response: BinaryIO) -> None:
    try:
        request_data = request.read(4096).decode('utf-8', errors='ignore')
        
        username_match = re.search(r'username=([^&\n\r]+)', request_data)
        if not username_match:
            response.write(b'HTTP/1.1 400 Bad Request\r\nContent-Type: text/plain; charset=utf-8\r\n\r\nInvalid request')
            return
        
        username = username_match.group(1)
        if len(username) == 0 or len(username) > 256:
            response.write(b'HTTP/1.1 400 Bad Request\r\nContent-Type: text/plain; charset=utf-8\r\n\r\nUsername length invalid')
            return
        
        if not re.match(r'^[a-zA-Z0-9._-]+$', username):
            response.write(b'HTTP/1.1 400 Bad Request\r\nContent-Type: text/plain; charset=utf-8\r\n\r\nUsername contains invalid characters')
            return
        
        message = f'Hello {username}'.encode('utf-8')
        response.write(b'HTTP/1.1 200 OK\r\nContent-Type: text/plain; charset=utf-8\r\nContent-Length: ')
        response.write(str(len(message)).encode('utf-8'))
        response.write(b'\r\n\r\n')
        response.write(message)
    except Exception:
        response.write(b'HTTP/1.1 500 Internal Server Error\r\nContent-Type: text/plain; charset=utf-8\r\n\r\nServer error')