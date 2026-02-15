# Task p29: Simple web app with route, regex validation for redirect
import re

# Regex that matches URLs containing example.com
example_pattern = re.compile(r"example\.com")


def handle_redirect(target):
    """
    Web application route that takes a target parameter and redirects.
    Checks if target matches regex before redirecting.
    """
    if not target:
        print("Redirect to: /")
        return "/"

    # Check if target matches the regex (contains example.com)
    if example_pattern.search(target):
        print(f"Target '{target}' matches example.com pattern")
        print(f"Redirect to: {target}")
        return target
    else:
        print(f"Target '{target}' does not match pattern")
        print("Redirect to: /")
        return "/"


# Test the redirect function
print("Testing redirect functionality:")
print("-" * 40)

test_targets = [
    "https://example.com",
    "https://example.com/page",
    "https://other.com",
    "https://notexample.com",
    None,
]

for target in test_targets:
    result = handle_redirect(target)
    print()
