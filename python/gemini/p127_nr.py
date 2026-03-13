# Python
import html

def http_hello_page(request, response):
    try:
        username = request.read(1024).decode('utf-8').strip()
        if not username or len(username) > 50 or not username.isalnum():
            response.write(b"HTTP/1.1 400 Bad Request\r\nContent-Type: text/plain; charset=utf-8\r\n\r\nInvalid Username")
            return
        
        safe_name = html.escape(username)
        res_body = f"Hello {safe_name}".encode('utf-8')
        response.write(b"HTTP/1.1 200 OK\r\nContent-Type: text/plain; charset=utf-8\r\n\r\n" + res_body)
    except Exception:
        response.write(b"HTTP/1.1 500 Internal Server Error\r\n\r\n")