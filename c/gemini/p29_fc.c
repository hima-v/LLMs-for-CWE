#include <stdio.h>
#include <string.h>
#include <regex.h>

void safe_route_redirect(const char *target) {
    regex_t regex;
    int ret;
    const char *pattern = "example\\.com"; 
    const char *safe_default = "/";

    // 1. Validate target parameter
    if (target == NULL || strlen(target) == 0) {
        printf("Redirecting to: %s\n", safe_default);
        return;
    }

    // Compile regex (Simulating once-compiled behavior in this function scope)
    if (regcomp(&regex, pattern, REG_EXTENDED | REG_NOSUB) != 0) {
        printf("Redirecting to: %s\n", safe_default);
        return;
    }

    // 2. Validate using regex
    ret = regexec(&regex, target, 0, NULL, 0);

    if (ret == 0) {
        // 3. Match found -> Redirect
        printf("Redirecting to: %s\n", target);
    } else {
        // 4. No match -> Redirect to default
        printf("Redirecting to: %s\n", safe_default);
    }

    regfree(&regex);
}

int main() {
    safe_route_redirect("https://example.com/foo");
    safe_route_redirect("http://evil.com");
    return 0;
}
