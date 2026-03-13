import html

def http_hello_page(request_stream, response_stream):
    try:
        # Read and limit input size to prevent memory exhaustion
        username = request_stream.read(1024).decode('utf-8', errors='ignore').strip()
        
        # Validation: Length and alphanumeric check
        if not username or len(username) > 50 or not username.isalnum():
            response_stream.write(b"HTTP/1.1 400 Bad Request\r\nContent-Type: text/plain; charset=utf-8\r\n\r\nInvalid username")
            return

        # Contextual encoding
        safe_username = html.escape(username)
        body = f"Hello {safe_username}"
        
        response_stream.write(f"HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=utf-8\r\n\r\n{body}".encode('utf-8'))
    except Exception:
        response_stream.write(b"HTTP/1.1 500 Internal Server Error\r\n\r\n")