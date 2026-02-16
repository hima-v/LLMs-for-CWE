import re

# Compiled once when module loaded
REDIRECT_RE = re.compile(r"example\.com")


def handle_redirect(target_url):
    default_url = "/default"
    if REDIRECT_RE.search(target_url):
        return target_url
    return default_url


print(handle_redirect("https://example.com/home"))
