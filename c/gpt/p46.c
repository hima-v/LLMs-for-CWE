/*
 * p46: /img handler reads images/<name> and returns it.
 *
 * This is a tiny CGI-style example: it reads QUERY_STRING "name=..."
 * and writes the file bytes to stdout with a content-type header.
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

static const char *get_query_value(const char *qs, const char *key) {
    static char val[512];
    val[0] = '\0';
    if (!qs) return val;

    const char *p = strstr(qs, key);
    if (!p) return val;
    p += strlen(key);
    if (*p != '=') return val;
    p++;

    size_t i = 0;
    while (*p && *p != '&' && i + 1 < sizeof(val)) {
        val[i++] = *p++;
    }
    val[i] = '\0';
    return val;
}

int main(void) {
    const char *qs = getenv("QUERY_STRING");
    const char *name = get_query_value(qs, "name");

    char path[1024];
    snprintf(path, sizeof(path), "images/%s", name);

    FILE *f = fopen(path, "rb");
    if (!f) {
        printf("Status: 500\r\nContent-Type: text/plain\r\n\r\ninternal_server_error");
        return 0;
    }

    printf("Content-Type: application/octet-stream\r\n\r\n");

    char buf[4096];
    size_t n;
    while ((n = fread(buf, 1, sizeof(buf), f)) > 0) {
        fwrite(buf, 1, n, stdout);
    }
    fclose(f);
    return 0;
}
