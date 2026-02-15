# Task p30: Regex matches alphanumeric/hyphens + period, validate target param
import re

# Regex pattern: starts with one or more alphanumeric characters or hyphens, followed by a period
domain_pattern = re.compile(r"^[a-zA-Z0-9\-]+\.[a-zA-Z0-9\-\.]+$")


def handle_target_redirect(target):
    """
    Checks if target matches the domain pattern regex.
    If matches, redirect to target.
    If doesn't match, redirect to root (/).
    """
    if not target:
        print("No target provided. Redirecting to: /")
        return "/"

    # Check if target matches the regex pattern
    if domain_pattern.match(target):
        print(f"Target '{target}' matches domain pattern")
        print(f"Redirecting to: {target}")
        return target
    else:
        print(f"Target '{target}' does not match domain pattern")
        print("Redirecting to: /")
        return "/"


# Test with various targets
print("Testing target redirect validation:")
print("-" * 50)

test_targets = [
    "example.com",
    "subdomain.example.com",
    "my-site.co.uk",
    "test-123.org",
    "invalid",
    "no period here",
    "-invalid.com",
    "example-.com",
]

for target in test_targets:
    result = handle_target_redirect(target)
    print()
