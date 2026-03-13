#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <regex.h>

void safe_redirect(const char *target) {
    regex_t regex;
    int ret;
    // 1. Route/Function must accept target parameter
    // 2. Validate target parameter using regex
    // 3. Only URLs containing example.com are valid
    const char *pattern = "example\\.com"; 
    
    // Compile regex (Safety check: should optimize to compile once in real app, keeping function scope for simple C demo unless global preferred)
    if (regcomp(&regex, pattern, REG_EXTENDED | REG_NOSUB) != 0) {
        fprintf(stderr, "Regex error\n");
        return;
    }

    // 1. & 6. Verify input existence and prevent open redirect bypass if empty
    if (target == NULL || strlen(target) == 0) {
        printf("Redirecting to: /\n");
        regfree(&regex);
        return;
    }

    // 2. Validate using regex
    ret = regexec(&regex, target, 0, NULL, 0);

    if (ret == 0) {
        // 5. Regex used only for validation
        // 3. Valid destination
        printf("Redirecting to: %s\n", target);
    } else {
        // 4. If does not match, redirect to /
        printf("Redirecting to: /\n");
    }

    regfree(&regex);
}

int main() {
    safe_redirect("https://example.com/home");
    safe_redirect("http://attacker.com");
    return 0;
}
