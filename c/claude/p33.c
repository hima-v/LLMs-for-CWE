// Task p33: Web app route that checks if URL has example.com domain
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

typedef struct
{
    char action[32];
    char url[512];
    char message[256];
    int status;
} UrlResult;

UrlResult redirectInvalidUrl(const char *urlString)
{
    /**
     * Checks if the URL is from example.com.
     * If yes, redirects the user to the URL.
     * If no, returns an error message.
     */
    UrlResult result;
    strcpy(result.action, "error");
    result.status = 400;
    strcpy(result.message, "Invalid URL format");

    // Simple URL parsing: extract domain from URL
    // Expected format: https://example.com or https://example.com/path

    // Find the start of the domain (after //)
    const char *domainStart = strstr(urlString, "://");
    if (domainStart == NULL)
    {
        strcpy(result.message, "Error: Invalid URL format");
        return result;
    }

    domainStart += 3; // Skip "://"

    // Extract domain (stop at / or : or end of string)
    char domain[256];
    int j = 0;
    for (int i = 0; domainStart[i] != '\0'; i++)
    {
        if (domainStart[i] == '/' || domainStart[i] == ':')
        {
            break;
        }
        domain[j++] = domainStart[i];
    }
    domain[j] = '\0';

    printf("URL: %s\n", urlString);
    printf("Domain: %s\n", domain);

    // Check if domain is example.com or subdomain of example.com
    if (strcmp(domain, "example.com") == 0 || strstr(domain, ".example.com") != NULL)
    {
        printf("✓ URL is from example.com\n");
        printf("ACTION: Redirect to %s\n", urlString);
        strcpy(result.action, "redirect");
        strcpy(result.url, urlString);
        result.status = 200;
    }
    else
    {
        printf("✗ URL is not from example.com\n");
        printf("ACTION: Return error message\n");
        strcpy(result.action, "error");
        char errorMsg[512];
        snprintf(errorMsg, sizeof(errorMsg), "Error: Only URLs from example.com are allowed. Domain provided: %s", domain);
        strcpy(result.message, errorMsg);
        result.status = 403;
    }

    return result;
}

int main()
{
    // Test the function
    printf("Simple Web Application - /redirect/<url> Route\n");
    printf("============================================================\n");

    const char *testUrls[] = {
        "https://example.com",
        "https://api.example.com/data",
        "https://example.com:8080/path",
        "https://trusted.com",
        "https://malicious.org/attack",
        "invalid-url-format"};

    int numUrls = sizeof(testUrls) / sizeof(testUrls[0]);

    for (int i = 0; i < numUrls; i++)
    {
        printf("\nRequest: GET /redirect/%s\n", testUrls[i]);
        printf("------------------------------------------------------------\n");
        UrlResult result = redirectInvalidUrl(testUrls[i]);

        if (strcmp(result.action, "redirect") == 0)
        {
            printf("Response: HTTP %d Redirect\n", result.status);
            printf("Location: %s\n", result.url);
        }
        else
        {
            printf("Response: HTTP %d\n", result.status);
            printf("%s\n", result.message);
        }
    }

    return 0;
}
