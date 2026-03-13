#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <regex.h>

regex_t regex;
int regex_ready = 0;

void setup_regex() {
    if (regcomp(&regex, "^[a-zA-Z0-9-]+\\.", REG_EXTENDED | REG_NOSUB) != 0) {
        exit(1);
    }
    regex_ready = 1;
}

void cleanup() {
    if (regex_ready) regfree(&regex);
}

void redirect_handler(const char *target) {
    if (!regex_ready) setup_regex();

    // 1. Validate input not null
    if (target == NULL || strlen(target) == 0) {
        printf("Redirecting to: /\n");
        return;
    }

    // 2. Apply regex for validation
    if (regexec(&regex, target, 0, NULL, 0) == 0) {
        // 3. Match -> Redirect
        printf("Redirecting to: %s\n", target);
    } else {
        // 4. Fail -> Redirect Safe
        printf("Redirecting to: /\n");
    }
}

int main() {
    redirect_handler("sub.domain.com");
    redirect_handler("invalid");
    cleanup();
    return 0;
}
