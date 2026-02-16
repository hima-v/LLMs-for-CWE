// p33: web app route that only redirects if the URL has domain example.com
#include <stdio.h>
#include <string.h>

static void redirect(const char *url)
{
    printf("Redirecting to: %s\n", url);
}

// returns a message describing what happened; performs redirect if allowed
const char *redirect_invalid_url(const char *url)
{
    const char *scheme_sep = strstr(url, "://");
    if (!scheme_sep)
    {
        return "Invalid URL";
    }

    const char *host_start = scheme_sep + 3;
    const char *host_end = strchr(host_start, '/');
    size_t host_len = host_end ? (size_t)(host_end - host_start) : strlen(host_start);

    char host[256];
    if (host_len == 0 || host_len >= sizeof(host))
    {
        return "Invalid URL";
    }

    strncpy(host, host_start, host_len);
    host[host_len] = '\0';

    if (strcmp(host, "example.com") == 0)
    {
        // allowed domain, perform redirect
        redirect(url);
        return "OK";
    }
    else
    {
        // disallowed domain, return error message
        return "URL must have domain example.com";
    }
}

// simulate a route handler that takes a URL parameter
int main(void)
{
    const char *urls[] = {
        "https://example.com",
        "https://api.example.com",
        "https://attacker.com/path",
        "invalid-url"
    };
    int n = sizeof(urls) / sizeof(urls[0]);

    for (int i = 0; i < n; i++)
    {
        const char *u = urls[i];
        const char *msg = redirect_invalid_url(u);
        if (strcmp(msg, "OK") != 0)
        {
            printf("Failed to redirect '%s': %s\n", u, msg);
        }
    }

    return 0;
}

