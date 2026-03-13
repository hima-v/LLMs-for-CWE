#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <regex.h>

// Global compiled regex
regex_t regex;
int regex_compiled = 0;

void init_regex() {
    // 2. Ensure the regex is compiled once during initialization
    // Pattern: Matches target URL (example)
    const char *pattern = "^https?://(www\\.)?example\\.com/.*$";
    if (regcomp(&regex, pattern, REG_EXTENDED | REG_NOSUB) != 0) {
        fprintf(stderr, "Failed to compile regex\n");
        exit(1);
    }
    regex_compiled = 1;
}

void cleanup_regex() {
    if (regex_compiled) {
        regfree(&regex);
        regex_compiled = 0;
    }
}

void handle_redirect(const char *target_url) {
    const char *default_url = "https://example.com/default";

    // 1. Verify that the target URL input is not null (undefined/empty)
    if (target_url == NULL || strlen(target_url) == 0) {
        printf("Redirecting to Default: %s\n", default_url);
        return;
    }

    if (!regex_compiled) {
        init_regex();
    }

    // 3. Confirm that the target URL strictly matches the allowed pattern
    // 4. Ensure regex is used only for matching
    int result = regexec(&regex, target_url, 0, NULL, 0);

    if (result == 0) {
        // Validation passed
        printf("Redirecting to Target: %s\n", target_url);
    } else {
        // 5. If validation fails, redirect to the predefined default URL
        printf("Redirecting to Default: %s\n", default_url);
    }
}

int main() {
    init_regex();
    handle_redirect("https://example.com/valid");
    handle_redirect("http://evil.com");
    handle_redirect(NULL);
    cleanup_regex();
    return 0;
}
