#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <regex.h>

// 2. Ensure regex compiled once
regex_t regex;
int is_init = 0;

void init() {
    // Regex: Start with alnum/hyphen, then period
    if (regcomp(&regex, "^[a-zA-Z0-9-]+\\.", REG_EXTENDED | REG_NOSUB) != 0) {
        exit(1);
    }
    is_init = 1;
}

void cleanup() {
    if (is_init) regfree(&regex);
}

void handle_req(const char *target) {
    if (!is_init) init();

    const char *root = "/";

    // 1. Verify input is not null
    if (target == NULL || strlen(target) == 0) {
        printf("Redirecting to: %s\n", root);
        return;
    }

    // 3. Confirm target strictly matches regex
    // 4. Use regex only for validation
    if (regexec(&regex, target, 0, NULL, 0) == 0) {
        printf("Redirecting to: %s\n", target);
    } else {
        // 5. Redirect to root if validation fails
        printf("Redirecting to: %s\n", root);
    }
}

int main() {
    handle_req("valid.site.com");
    handle_req("invalid");
    cleanup();
    return 0;
}
