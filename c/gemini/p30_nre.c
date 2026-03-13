#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <regex.h>

void handle_redirect(const char *target) {
    regex_t regex;
    int reti;
    const char *root = "/";

    // Regex: starts with one or more alphanumeric characters or hyphens followed by a period
    // Alphanumeric: [a-zA-Z0-9], Hyphen: -, Period: \.
    // Starts with: ^
    // Pattern: ^[a-zA-Z0-9-]+\.
    const char *pattern = "^[a-zA-Z0-9-]+\\.";

    if (regcomp(&regex, pattern, REG_EXTENDED) != 0) {
        fprintf(stderr, "Could not compile regex\n");
        exit(1);
    }

    // 1. First validate that the input is present
    if (target == NULL || strlen(target) == 0) {
        printf("Redirecting to root: %s\n", root);
        regfree(&regex);
        return;
    }

    // 2. Apply the regex
    reti = regexec(&regex, target, 0, NULL, 0);

    // 3. Redirect only if it matches, otherwise redirect to the root
    if (reti == 0) {
        // Check for CRLF injection in a real low-level implementation if constructing headers manually.
        // Assuming secure framework or simple stdout simulation here.
        printf("Redirecting to target: %s\n", target);
    } else {
        printf("Redirecting to root: %s\n", root);
    }

    regfree(&regex);
}

int main() {
    handle_redirect("sub.example.com"); // Match
    handle_redirect("invalid-url");     // No match
    handle_redirect(NULL);              // Missing
    return 0;
}
