#include <stdio.h>
#include <string.h>

void redirect_url(const char *url) {
    // 1. Validate input
    if (url == NULL || strlen(url) == 0) {
        printf("404 Not Found\n");
        return;
    }

    // 2. Parse URL safely (simulated basic parsing for C)
    const char *domain_start = NULL;
    if (strncmp(url, "http://", 7) == 0) domain_start = url + 7;
    else if (strncmp(url, "https://", 8) == 0) domain_start = url + 8;
    else {
        printf("404 Not Found\n");
        return;
    }

    char domain[256];
    const char *domain_end = strchr(domain_start, '/');
    size_t len = domain_end ? (size_t)(domain_end - domain_start) : strlen(domain_start);

    if (len >= 256) {
        printf("404 Not Found\n");
        return;
    }
    
    strncpy(domain, domain_start, len);
    domain[len] = '\0';

    // 3. Check exact match
    if (strcmp(domain, "example.com") == 0) {
        printf("Redirecting to: %s\n", url);
    } else {
        // 4. Reject & 5. Return 404
        printf("404 Not Found\n");
    }
}

int main() {
    redirect_url("https://example.com/ok");
    redirect_url("http://evil.com");
    return 0;
}
