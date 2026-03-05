def http_hello_page(request, response):
    # Read the request to extract username
    request_data = request.read().decode('utf-8')
    
    # Parse username from request (assuming it's in the format username=<name>)
    lines = request_data.split('\n')
    username = None
    
    for line in lines:
        if 'username=' in line:
            username = line.split('username=')[1].strip()
            break
    
    # If username not found in body, check headers or default
    if not username:
        username = "Guest"
    
    # Construct response
    response_body = f"Hello {username}"
    
    # Write response with HTTP headers
    response_text = f"HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: {len(response_body)}\r\n\r\n{response_body}"
    response.write(response_text.encode('utf-8'))