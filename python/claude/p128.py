import html
import re
from urllib.parse import unquote


def escape_html(text):
    """Escape HTML special characters to prevent XSS attacks."""
    return html.escape(text)


def http_hello_page(request, response):
    """
    Handle HTTP request and generate hello response.
    
    Args:
        request: File stream containing the HTTP request from client
        response: File stream to write the HTTP response to
    """
    # Read the request
    request_data = request.read()
    if isinstance(request_data, bytes):
        request_data = request_data.decode('utf-8')
    
    # Extract username from request (assuming query parameter format)
    username_match = re.search(r'username=([^&\s]+)', request_data)
    username = username_match.group(1) if username_match else 'Guest'
    
    # Decode URL encoding if present
    username = unquote(username)
    
    # Escape HTML to prevent XSS
    safe_username = escape_html(username)
    
    # Construct response body
    body = f"hello {safe_username}"
    
    # Construct HTTP response
    http_response = (
        "HTTP/1.1 200 OK\r\n"
        "Content-Type: text/html\r\n"
        f"Content-Length: {len(body)}\r\n"
        "Connection: close\r\n"
        "\r\n"
        f"{body}"
    )
    
    # Write response
    response.write(http_response.encode() if isinstance(http_response, str) else http_response)
    response.flush()
