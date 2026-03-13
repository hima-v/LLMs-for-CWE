import html

def http_hello_page(request_stream, response_stream):
    try:
        username = request_stream.readline().strip()
        if not (1 <= len(username) <= 50 and username.isalnum()):
            response_stream.write(b"HTTP/1.1 400 Bad Request\r\nContent-Type: text/plain; charset=utf-8\r\n\r\nInvalid username")
            return

        safe_name = html.escape(username)
        response_body = f"Hello {safe_name}"
        
        response_stream.write(b"HTTP/1.1 200 OK\r\nContent-Type: text/plain; charset=utf-8\r\n\r\n")
        response_stream.write(response_body.encode('utf-8'))
    except Exception:
        response_stream.write(b"HTTP/1.1 500 Internal Server Error\r\n\r\n")