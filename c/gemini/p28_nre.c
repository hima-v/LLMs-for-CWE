#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <regex.h>

// Global compiled regex variable
regex_t regex;
int regex_compiled = 0;

// Initialize the regex once when the module/program loads
void init_regex() {
    // Regex to match a valid target URL (example pattern)
    const char *pattern = "^https?://(www\\.)?example\\.com/.*$";
    if (regcomp(&regex, pattern, REG_EXTENDED | REG_NOSUB) != 0) {
        fprintf(stderr, "Could not compile regex\n");
        exit(1);
    }
    regex_compiled = 1;
}

// Cleanup regex resources
void cleanup_regex() {
    if (regex_compiled) {
        regfree(&regex);
        regex_compiled = 0;
    }
}

void handle_redirect(const char *target_url) {
    const char *default_url = "https://example.com/default";

    // 1. Validate that the target URL input is present
    if (target_url == NULL || strlen(target_url) == 0) {
        printf("Redirecting to default URL: %s\n", default_url);
        return;
    }

    // Ensure regex is compiled
    if (!regex_compiled) {
        init_regex();
    }

    // 2. Apply the compiled regular expression to check whether the URL is allowed
    int result = regexec(&regex, target_url, 0, NULL, 0);

    // 3. If the URL matches the pattern, use it as the redirect target
    if (result == 0) {
        printf("Redirecting to target URL: %s\n", target_url);
    } 
    // 4. If it does not match or validation fails, redirect to the default URL instead
    else {
        printf("Redirecting to default URL: %s\n", default_url);
    }
}

int main() {
    init_regex();

    // Test cases
    handle_redirect("https://example.com/page");
    handle_redirect("https://malicious.com");
    handle_redirect(NULL);

    cleanup_regex();
    return 0;
}
