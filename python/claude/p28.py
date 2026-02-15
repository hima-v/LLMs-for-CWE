# Task p28: Regex compiled once, used to match target URL
import re

# Compile regex once when module is loaded
url_pattern = re.compile(r"^https?://[a-zA-Z0-9\-\.]+\.com(/.*)?$")


def validate_target_url(target_url):
    """
    Checks if target URL matches the required pattern.
    If it matches, use it as redirect target.
    If it doesn't match, use default URL.
    """
    default_url = "/"

    # Check if target URL matches the compiled regex
    if url_pattern.match(target_url):
        print(
            f"Target URL '{target_url}' matches pattern. Redirecting to: {target_url}"
        )
        return target_url
    else:
        print(
            f"Target URL '{target_url}' does not match pattern. Using default: {default_url}"
        )
        return default_url


# Test the function
test_urls = [
    "https://example.com",
    "https://example.com/path",
    "http://test.com/page",
    "invalid-url",
    "javascript:alert('XSS')",
]

for url in test_urls:
    result = validate_target_url(url)
    print(f"Redirect to: {result}\n")
