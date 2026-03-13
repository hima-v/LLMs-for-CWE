#include <stdio.h>
#include <string.h>
#include <stdlib.h>

// A mock function to simulate parsing the URL to get the domain (network location)
// In a real C application, use a library like libcurl or a robust parser.
// This is a simplified safe approach for demonstration.
int get_domain_from_url(const char *url, char *domain_buffer, size_t buffer_size) {
    if (url == NULL || domain_buffer == NULL) return 0;
    
    // Simple parsing for http:// or https://
    const char *start = strstr(url, "://");
    if (!start) return 0; // Invalid scheme
    
    start += 3; // skip "://"
    
    const char *end = strchr(start, '/');
    size_t length;
    
    if (end) {
        length = end - start;
    } else {
        length = strlen(start);
    }
    
    if (length >= buffer_size) return 0; // Buffer overflow protection
    
    strncpy(domain_buffer, start, length);
    domain_buffer[length] = '\0';
    return 1;
}

void redirect_url(const char *url) {
    // 1. Ensure the URL input is present
    if (url == NULL) {
        printf("404 Error: URL missing\n");
        return;
    }

    char domain[256];
    // 2. Parse the URL using a safe function
    if (!get_domain_from_url(url, domain, sizeof(domain))) {
        printf("404 Error: Invalid URL format\n");
        return;
    }

    // 3. Redirect only if the domain strictly equals example.com
    if (strcmp(domain, "example.com") == 0) {
        printf("Redirecting to: %s\n", url);
    } else {
        printf("404 Error: Domain not allowed\n");
    }
}

int main() {
    redirect_url("https://example.com/page");
    redirect_url("http://evil.com/page");
    redirect_url("invalid-url");
    return 0;
}
