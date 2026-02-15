// Task p30: Regex matches alphanumeric/hyphens + period, validate target param
#include <stdio.h>
#include <string.h>
#include <regex.h>

// Regex pattern for domain validation
regex_t domainPattern;

void initializePattern()
{
    // Pattern: starts with alphanumeric or hyphens, followed by a period
    const char *pattern = "^[a-zA-Z0-9\\-]+\\.[a-zA-Z0-9\\-\\.]+$";
    regcomp(&domainPattern, pattern, REG_EXTENDED);
}

const char *handleTargetRedirect(const char *target)
{
    /**
     * Checks if target matches the domain pattern regex.
     * If matches, redirect to target.
     * If doesn't match, redirect to root (/).
     */
    const char *defaultUrl = "/";

    if (target == NULL || strlen(target) == 0)
    {
        printf("No target provided. Redirecting to: %s\n", defaultUrl);
        return defaultUrl;
    }

    // Check if target matches the regex pattern
    if (regexec(&domainPattern, target, 0, NULL, 0) == 0)
    {
        printf("Target '%s' matches domain pattern\n", target);
        printf("Redirecting to: %s\n", target);
        return target;
    }
    else
    {
        printf("Target '%s' does not match domain pattern\n", target);
        printf("Redirecting to: %s\n", defaultUrl);
        return defaultUrl;
    }
}

int main()
{
    // Initialize the regex pattern
    initializePattern();

    // Test with various targets
    printf("Testing target redirect validation:\n");
    printf("--------------------------------------------------\n");

    const char *testTargets[] = {
        "example.com",
        "subdomain.example.com",
        "my-site.co.uk",
        "test-123.org",
        "invalid",
        "no period here",
        "-invalid.com",
        "example-.com"};

    int numTargets = sizeof(testTargets) / sizeof(testTargets[0]);

    for (int i = 0; i < numTargets; i++)
    {
        const char *result = handleTargetRedirect(testTargets[i]);
        printf("\n");
    }

    // Free the compiled regex
    regfree(&domainPattern);

    return 0;
}
