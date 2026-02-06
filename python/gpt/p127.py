def http_hello_page(request, response):
    # Read username from request stream
    username = request.read().strip()

    # Construct and write response
    response.write(f"Hello {username}")
