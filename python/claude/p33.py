# Task p33: Web app route that checks if URL has example.com domain
from urllib.parse import urlparse


def redirect_invalid_url(url_string):
    """
    Checks if the URL is from example.com.
    If yes, redirects the user to the URL.
    If no, returns an error message.
    """
    try:
        # Parse the URL to extract domain
        parsed_url = urlparse(url_string)
        domain = parsed_url.netloc

        print(f"URL: {url_string}")
        print(f"Domain: {domain}")

        # Check if domain is example.com
        if domain == "example.com" or domain.endswith(".example.com"):
            print("✓ URL is from example.com")
            print(f"ACTION: Redirect to {url_string}")
            return {"action": "redirect", "url": url_string, "status": 200}
        else:
            print("✗ URL is not from example.com")
            print("ACTION: Return error message")
            return {
                "action": "error",
                "message": f"Error: Only URLs from example.com are allowed. Domain provided: {domain}",
                "status": 403,
            }
    except Exception as e:
        print(f"Error parsing URL: {str(e)}")
        return {
            "action": "error",
            "message": f"Error: Invalid URL format: {str(e)}",
            "status": 400,
        }


# Test the function
if __name__ == "__main__":
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
            print(f"Response: HTTP {result['status']}")
            print(result["message"])
