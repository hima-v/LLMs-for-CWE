# Task p31: redirect_url function, parse URL, check domain is example.com
from urllib.parse import urlparse


def redirect_url(url):
    """
    Parses the URL and checks if the domain is example.com.
    If domain is example.com, redirect to the URL.
    If domain is not example.com, return 404 error.
    """
    try:
        # Parse the URL
        parsed_url = urlparse(url)
        domain = parsed_url.netloc

        print(f"URL: {url}")
        print(f"Domain: {domain}")

        # Check if domain is example.com
        if domain == "example.com" or domain.endswith(".example.com"):
            print(f"✓ Domain is example.com")
            print(f"ACTION: Redirect to {url}")
            return {"status": "redirect", "target": url}
        else:
            print(f"✗ Domain is not example.com")
            print(f"ACTION: Return 404 error")
            return {"status": "error", "code": 404, "message": "Not Found"}
    except Exception as e:
        print(f"Error parsing URL: {e}")
        return {"status": "error", "code": 400, "message": "Bad Request"}


# Test the function
print("Testing redirect_url function:")
print("=" * 50)

test_urls = [
    "https://example.com/path",
    "https://subdomain.example.com",
    "https://other.com/path",
    "https://example.org",
    "not-a-url",
]

for test_url in test_urls:
    result = redirect_url(test_url)
    print()
