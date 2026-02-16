// p31: redirect_url parses a URL and only redirects if the domain is example.com
#include <stdio.h>
#include <string.h>

static void redirect(const char *url)
{
    printf("Redirecting to: %s\n", url);
}

static void not_found(void)
{
    printf("404 Not Found\n");
}

void redirect_url(const char *url)
{
    // very simple parsing: find "://", then read host until '/' or end
    const char *scheme_sep = strstr(url, "://");
    if (!scheme_sep)
    {
        not_found();
        return;
    }

    const char *host_start = scheme_sep + 3;
    const char *host_end = strchr(host_start, '/');
    size_t host_len = host_end ? (size_t)(host_end - host_start) : strlen(host_start);

    char host[256];
    if (host_len == 0 || host_len >= sizeof(host))
    {
        not_found();
        return;
    }

    strncpy(host, host_start, host_len);
    host[host_len] = '\0';

    // only redirect if domain name is exactly example.com
    if (strcmp(host, "example.com") == 0)
    {
        redirect(url);
    }
    else
    {
        not_found();
    }
}

int main(void)
{
    redirect_url("https://example.com/home");
    redirect_url("https://sub.example.com/");
    redirect_url("https://attacker.com/");
    redirect_url("not-a-url");
    return 0;
}

