#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <regex.h>

// Global compiled regex
regex_t regex;
int regex_compiled = 0;

void init_regex() {
    // 2. Compile regex once
    if (regcomp(&regex, "^https?://(www\\.)?example\\.com/.*$", REG_EXTENDED | REG_NOSUB) != 0) {
        fprintf(stderr, "Regex compilation failed\n");
        exit(1);
    }
    regex_compiled = 1;
}

void process_redirect(const char *target_url) {
    const char *default_url = "https://example.com/default";

    // 1. Validate input is not null/empty
    if (target_url == NULL || strlen(target_url) == 0) {
        printf("Redirecting to default: %s\n", default_url);
        return;
    }

    if (!regex_compiled) init_regex();

    // 2. Apply regex only for validation
    // 3. Allow redirect only if matches
    if (regexec(&regex, target_url, 0, NULL, 0) == 0) {
        printf("Redirecting to: %s\n", target_url);
    } else {
        // 4. Reject invalid input
        // 5. Safe redirect to default
        printf("Redirecting to default: %s\n", default_url);
    }
}

int main() {
    init_regex();
    process_redirect("https://example.com/page");
    process_redirect("http://evil.com");
    regfree(&regex);
    return 0;
}
