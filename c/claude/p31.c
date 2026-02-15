// Task p31: redirect_url function, parse URL, check domain is example.com
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

typedef struct
{
    int status;
    char action[32];
    char target[256];
    int code;
    char message[256];
} UrlResult;

UrlResult redirectUrl(const char *urlString)
{
    /**
     * Parses the URL and checks if the domain is example.com.
     * If domain is example.com, redirect to the URL.
     * If domain is not example.com, return 404 error.
     */
    UrlResult result;
    strcpy(result.action, "error");
    result.code = 400;
    strcpy(result.message, "Bad Request");

    // Simple URL parsing: extract domain from URL
    // Expected format: https://example.com or https://example.com/path

    // Find the start of the domain (after //)
    const char *domainStart = strstr(urlString, "://");
    if (domainStart == NULL)
    {
        strcpy(result.message, "Invalid URL format");
        return result;
    }

    domainStart += 3; // Skip "://"

    // Find the end of the domain (/ or end of string)
    const char *domainEnd = strchr(domainStart, '/');
    if (domainEnd == NULL)
    {
        domainEnd = urlString + strlen(urlString);
    }

    // Extract domain
    char domain[256];
    int domainLen = domainEnd - domainStart;
    strncpy(domain, domainStart, domainLen);
    domain[domainLen] = '\0';

    // Remove port if present
    char *colonPos = strchr(domain, ':');
    if (colonPos != NULL)
    {
        *colonPos = '\0';
    }

    printf("URL: %s\n", urlString);
    printf("Domain: %s\n", domain);

    // Check if domain is example.com or subdomain of example.com
    if (strcmp(domain, "example.com") == 0 || strstr(domain, ".example.com") != NULL)
    {
        printf("✓ Domain is example.com\n");
        printf("ACTION: Redirect to %s\n", urlString);
        strcpy(result.action, "redirect");
        result.status = 200;
        strcpy(result.target, urlString);
    }
    else
    {
        printf("✗ Domain is not example.com\n");
        printf("ACTION: Return 404 error\n");
        strcpy(result.action, "error");
        result.code = 404;
        strcpy(result.message, "Not Found");
    }

    return result;
}

int main()
{
    // Test the function
    printf("Testing redirectUrl function:\n");
    printf("==================================================\n");

    const char *testUrls[] = {
        "https://example.com/path",
        "https://subdomain.example.com",
        "https://other.com/path",
        "https://example.org",
        "not-a-url"};

    int numUrls = sizeof(testUrls) / sizeof(testUrls[0]);

    for (int i = 0; i < numUrls; i++)
    {
        UrlResult result = redirectUrl(testUrls[i]);
        printf("\n");
    }

    return 0;
}
