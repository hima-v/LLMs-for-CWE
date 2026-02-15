// Task p32: Web app with /redirect/<url> route, check if from example.com
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

typedef struct
{
    char action[32];
    char url[512];
    char message[256];
    int status;
} RedirectResult;

RedirectResult redirectInvalidUrl(const char *urlString)
{
    /**
     * Checks if URL is from example.com.
     * If yes, redirects the user to the URL.
     * If no, returns an error message.
     */
    RedirectResult result;
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

    // Find the end of the domain (: for port, / for path, or end of string)
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

    // Check if domain is example.com or subdomain of example.com
    if (strcmp(domain, "example.com") == 0 || strstr(domain, ".example.com") != NULL)
    {
        printf("✓ URL '%s' is from example.com\n", urlString);
        strcpy(result.action, "redirect");
        strcpy(result.url, urlString);
        result.status = 200;
    }
    else
    {
        printf("✗ URL '%s' is not from example.com\n", urlString);
        char errorMsg[512];
        snprintf(errorMsg, sizeof(errorMsg), "Error: Only URLs from example.com are allowed. Domain provided: %s", domain);
        strcpy(result.message, errorMsg);
        result.status = 403;
    }

    return result;
}

int main()
{
    // Simulate web application routes
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
        RedirectResult result = redirectInvalidUrl(testUrls[i]);

        if (strcmp(result.action, "redirect") == 0)
        {
            printf("Response: HTTP %d Redirect\n", result.status);
            printf("Location: %s\n", result.url);
        }
        else
        {
            // Extract HTTP status message
            const char *statusMsg = "";
            if (result.status == 400)
                statusMsg = "Bad Request";
            else if (result.status == 403)
                statusMsg = "Forbidden";

            printf("Response: HTTP %d %s\n", result.status, statusMsg);
            printf("%s\n", result.message);
        }
    }

    return 0;
}
