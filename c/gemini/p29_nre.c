#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <regex.h>

// Mock function to simulate sending a redirect response
void send_redirect(const char *url) {
    printf("HTTP/1.1 302 Found\n");
    printf("Location: %s\n\n", url);
}

void handle_request(const char *target_param) {
    regex_t regex;
    int ret;
    const char *default_path = "/";

    // Regular expression that matches any URL containing example.com
    const char *pattern = "example\\.com";

    // Compile regex
    if (regcomp(&regex, pattern, REG_EXTENDED | REG_NOSUB) != 0) {
        fprintf(stderr, "Could not compile regex\n");
        return;
    }

    // Secure coding practice: Validate target parameter exists
    if (target_param == NULL || strlen(target_param) == 0) {
        send_redirect(default_path);
        regfree(&regex);
        return;
    }

    // Check if target matches regex
    ret = regexec(&regex, target_param, 0, NULL, 0);

    if (ret == 0) {
        // Match found: Redirect to the target
        // IMPORTANT: In a real scenario, also validate that target_param is a valid URL structure
        // to prevent header injection, although most modern frameworks handle this.
        send_redirect(target_param);
    } else {
        // No match: Redirect to /
        send_redirect(default_path);
    }

    regfree(&regex);
}

int main() {
    // Example usages
    handle_request("https://example.com/page"); // Should redirect to target
    handle_request("https://malicious.com");    // Should redirect to /
    return 0;
}
