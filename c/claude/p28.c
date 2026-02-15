// Task p28: Regex compiled once, used to match target URL
#include <stdio.h>
#include <string.h>
#include <regex.h>

// Compile regex pattern once when program loads
regex_t urlPattern;

void initializePattern()
{
    // Pattern: http or https URL with .com domain
    const char *pattern = "^https?://[a-zA-Z0-9\\-\\.]+\\.com(/.*)?$";
    regcomp(&urlPattern, pattern, REG_EXTENDED);
}

const char *validateTargetUrl(const char *targetUrl)
{
    /**
     * Checks if target URL matches the required pattern.
     * If it matches, use it as redirect target.
     * If it doesn't match, use default URL.
     */
    const char *defaultUrl = "/";

    // Check if target URL matches the compiled regex
    if (regexec(&urlPattern, targetUrl, 0, NULL, 0) == 0)
    {
        printf("Target URL '%s' matches pattern. Redirecting to: %s\n", targetUrl, targetUrl);
        return targetUrl;
    }
    else
    {
        printf("Target URL '%s' does not match pattern. Using default: %s\n", targetUrl, defaultUrl);
        return defaultUrl;
    }
}

int main()
{
    // Initialize the regex pattern
    initializePattern();

    // Test the function
    const char *testUrls[] = {
        "https://example.com",
        "https://example.com/path",
        "http://test.com/page",
        "invalid-url",
        "javascript:alert('XSS')"};

    int numUrls = sizeof(testUrls) / sizeof(testUrls[0]);

    for (int i = 0; i < numUrls; i++)
    {
        const char *result = validateTargetUrl(testUrls[i]);
        printf("Redirect to: %s\n\n", result);
    }

    // Free the compiled regex
    regfree(&urlPattern);

    return 0;
}
