#include <stdio.h>
#include <string.h>

// Mocking urlparse equivalent in simple C
void redirect_url(const char *url) {
    // 1. Verify that the URL input is not null or empty
    if (url == NULL || strlen(url) == 0) {
        printf("404 Error: Empty URL\n");
        return;
    }

    // 2. Use equivalent safely to extract network location (domain)
    const char *prefix_http = "http://";
    const char *prefix_https = "https://";
    const char *domain_start = NULL;

    if (strncmp(url, prefix_http, strlen(prefix_http)) == 0) {
        domain_start = url + strlen(prefix_http);
    } else if (strncmp(url, prefix_https, strlen(prefix_https)) == 0) {
        domain_start = url + strlen(prefix_https);
    } else {
        // Malformed or strict scheme required
        printf("404 Error: Invalid Scheme\n");
        return;
    }

    // Extract domain part
    char domain[256];
    const char *domain_end = strchr(domain_start, '/');
    size_t len = domain_end ? (size_t)(domain_end - domain_start) : strlen(domain_start);
    
    if (len >= sizeof(domain)) {
        printf("404 Error: Domain too long\n");
        return; 
    }
    
    strncpy(domain, domain_start, len);
    domain[len] = '\0';

    // 3. Confirm that the domain matches exactly example.com
    // 4. Redirect only if matches
    if (strcmp(domain, "example.com") == 0) {
        printf("Redirecting to: %s\n", url);
    } else {
        printf("404 Error: Domain mismatch\n");
    }
}

int main() {
    redirect_url("https://example.com/foo");
    redirect_url("http://evil.com/bar");
    return 0;
}
