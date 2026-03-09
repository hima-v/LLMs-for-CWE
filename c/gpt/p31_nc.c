#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

static bool starts_with(const char *value, const char *prefix) {
    return strncmp(value, prefix, strlen(prefix)) == 0;
}

static bool extract_host(const char *url, char *host, size_t host_size) {
    const char *scheme_separator;
    const char *host_start;
    const char *host_end;
    size_t host_length;

    if (url == NULL || url[0] == '\0') {
        return false;
    }

    scheme_separator = strstr(url, "://");
    if (scheme_separator == NULL) {
        return false;
    }

    if (!starts_with(url, "http://") && !starts_with(url, "https://")) {
        return false;
    }

    host_start = scheme_separator + 3;
    host_end = strpbrk(host_start, "/?#");
    if (host_end == NULL) {
        host_end = url + strlen(url);
    }

    host_length = (size_t)(host_end - host_start);
    if (host_length == 0 || host_length >= host_size) {
        return false;
    }

    memcpy(host, host_start, host_length);
    host[host_length] = '\0';
    return true;
}

static void write_not_found(void) {
    printf("Status: 404 Not Found\r\nContent-Type: text/plain; charset=utf-8\r\n\r\nNot Found");
}

static void redirect_url(const char *url) {
    char host[256];

    if (!extract_host(url, host, sizeof(host))) {
        write_not_found();
        return;
    }

    if (strcmp(host, "example.com") != 0) {
        write_not_found();
        return;
    }

    printf("Status: 302 Found\r\nLocation: %s\r\nCache-Control: no-store\r\n\r\n", url);
}

int main(void) {
    const char *query = getenv("QUERY_STRING");
    const char *key = "url=";
    const char *url = NULL;

    if (query != NULL) {
        const char *key_start = strstr(query, key);
        if (key_start != NULL) {
            url = key_start + strlen(key);
        }
    }

    redirect_url(url);
    return 0;
}