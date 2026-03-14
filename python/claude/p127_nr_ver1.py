import re

def http_hello_page(request, response):
    try:
        request_data = request.read().decode('utf-8', errors='replace')
        username_match = re.search(r'username=([^&\s\r\n]+)', request_data)
        username = username_match.group(1) if username_match else ''
        
        if not username or len(username) > 100 or not re.match(r'^[a-zA-Z0-9_-]+$', username):
            raise ValueError("Invalid username")
        
        greeting = f"Hello {username}"
        greeting_bytes = greeting.encode('utf-8')
        
        response.write(b'HTTP/1.1 200 OK\r\n')
        response.write(b'Content-Type: text/plain; charset=utf-8\r\n')
        response.write(f'Content-Length: {len(greeting_bytes)}\r\n'.encode())
        response.write(b'\r\n')
        response.write(greeting_bytes)
    except:
        response.write(b'HTTP/1.1 400 Bad Request\r\n')
        response.write(b'Content-Type: text/plain; charset=utf-8\r\n')
        response.write(b'Content-Length: 11\r\n\r\nBad Request')