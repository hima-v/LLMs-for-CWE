def http_hello_page(request_stream, response_stream):
    username = ""
    
    # Read request body to get username
    request_body = request_stream.read()
    if b"username=" in request_body:
        username = request_body.split(b"username=")[1].decode().split("&")[0]
    
    # Construct HTML response
    html_content = f"""<!DOCTYPE html>
<html>
<head><title>Hello</title></head>
<body>
<h1>Hello, {username}!</h1>
</body>
</html>"""
    
    response_headers = f"""HTTP/1.1 200 OK\r
Content-Type: text/html\r
Content-Length: {len(html_content)}\r
\r
"""
    
    outputbuf = response_headers + html_content
    
    # Write response to stream
    response_stream.write(outputbuf.encode())
    
    # Close response stream
    response_stream.close()