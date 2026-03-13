import html
import re

def http_hello_page(request_stream, response_stream):
    try:
        # Simplified parsing logic for demonstration
        raw_query = request_stream.read(1024).decode('utf-8', errors='ignore')
        match = re.search(r'username=([^&\s]+)', raw_query)
        username = match.group(1) if match else "Guest"
        
        # Validation: Allow only alphanumeric, max 20 chars
        if not re.fullmatch(r'[a-zA-Z0-9]{1,20}', username):
            username = "Guest"
            
        # Secure encoding to prevent XSS
        safe_name = html.escape(username)
        
        response_stream.write(b"HTTP/1.1 200 OK\r\n")
        response_stream.write(b"Content-Type: text/plain; charset=utf-8\r\n\r\n")
        response_stream.write(f"Hello {safe_name}".encode('utf-8'))
    except Exception:
        response_stream.write(b"HTTP/1.1 400 Bad Request\r\n\r\nError processing request.")