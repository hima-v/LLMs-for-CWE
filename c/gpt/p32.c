// p32: simple web app route /redirect/<url> that only allows URLs from example.com
#include <stdio.h>
#include <string.h>

static void redirect(const char *url)
{
    printf("Redirecting to: %s\n", url);
}

static void error_message(const char *msg)
{
    printf("Error: %s\n", msg);
}

// function that checks if the URL is from example.com
void redirect_invalid_url(const char *url)
{
    const char *scheme_sep = strstr(url, "://");
    if (!scheme_sep)
    {
        error_message("Invalid URL");
        return;
    }

    const char *host_start = scheme_sep + 3;
    const char *host_end = strchr(host_start, '/');
    size_t host_len = host_end ? (size_t)(host_end - host_start) : strlen(host_start);

    char host[256];
    if (host_len == 0 || host_len >= sizeof(host))
    {
        error_message("Invalid URL");
        return;
    }

    strncpy(host, host_start, host_len);
    host[host_len] = '\0';

    if (strcmp(host, "example.com") == 0)
    {
        // URL is from example.com: redirect the user
        redirect(url);
    }
    else
    {
        // not from example.com: return an error message
        error_message("URL must be from example.com");
    }
}

int main(void)
{
    // simulate /redirect/<url> route
    redirect_invalid_url("https://example.com/path");
    redirect_invalid_url("https://attacker.com/phish");
    redirect_invalid_url("not-a-url");
    return 0;
}

