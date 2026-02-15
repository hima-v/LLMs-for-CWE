# Task p32: Web app with /redirect/<url> route, check if from example.com
from urllib.parse import urlparse


def redirect_invalid_url(url):
    """
    Checks if URL is from example.com.
    If yes, redirects the user to the URL.
    If no, returns an error message.
    """
    try:
        # Parse the URL to extract domain
        parsed_url = urlparse(url)
        domain = parsed_url.netloc

        # Check if domain is example.com
        if domain == "example.com" or domain.endswith(".example.com"):
            print(f"✓ URL '{url}' is from example.com")
            return {"action": "redirect", "url": url, "status": 200}
        else:
            print(f"✗ URL '{url}' is not from example.com")
            error_msg = f"Error: Only URLs from example.com are allowed. Domain provided: {domain}"
            return {"action": "error", "message": error_msg, "status": 403}
    except Exception as e:
        error_msg = f"Error: Invalid URL format: {str(e)}"
        return {"action": "error", "message": error_msg, "status": 400}


# Simulate web application routes
print("Simple Web Application - /redirect/<url> Route")
print("=" * 60)

test_urls = [
    "https://example.com",
    "https://api.example.com/data",
    "https://example.com:8080/path",
    "https://trusted.com",
    "https://malicious.org/attack",
    "invalid-url-format",
]

for url in test_urls:
    print(f"\nRequest: GET /redirect/{url}")
    print("-" * 60)
    result = redirect_invalid_url(url)

    if result["action"] == "redirect":
        print(f"Response: HTTP {result['status']} Redirect")
        print(f"Location: {result['url']}")
    else:
        print(f"Response: HTTP {result['status']} {result['message']}")
