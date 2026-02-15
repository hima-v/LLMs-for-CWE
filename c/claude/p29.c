// Task p29: Simple web app with route, regex validation for redirect
#include <stdio.h>
#include <string.h>
#include <regex.h>

// Regex pattern that matches URLs containing example.com
regex_t examplePattern;

void initializePattern()
{
    const char *pattern = "example\\.com";
    regcomp(&examplePattern, pattern, REG_EXTENDED);
}

const char *handleRedirect(const char *target)
{
    /**
     * Web application route that takes a target parameter and redirects.
     * Checks if target matches regex before redirecting.
     */
    const char *defaultUrl = "/";

    if (target == NULL || strlen(target) == 0)
    {
        printf("Redirect to: %s\n", defaultUrl);
        return defaultUrl;
    }

    // Check if target matches the regex (contains example.com)
    if (regexec(&examplePattern, target, 0, NULL, 0) == 0)
    {
        printf("Target '%s' matches example.com pattern\n", target);
        printf("Redirect to: %s\n", target);
        return target;
    }
    else
    {
        printf("Target '%s' does not match pattern\n", target);
        printf("Redirect to: %s\n", defaultUrl);
        return defaultUrl;
    }
}

int main()
{
    // Initialize the regex pattern
    initializePattern();

    // Test the redirect function
    printf("Testing redirect functionality:\n");
    printf("----------------------------------------\n");

    const char *testTargets[] = {
        "https://example.com",
        "https://example.com/page",
        "https://other.com",
        "https://notexample.com",
        ""};

    int numTargets = sizeof(testTargets) / sizeof(testTargets[0]);

    for (int i = 0; i < numTargets; i++)
    {
        const char *result = handleRedirect(testTargets[i]);
        printf("\n");
    }

    // Free the compiled regex
    regfree(&examplePattern);

    return 0;
}
