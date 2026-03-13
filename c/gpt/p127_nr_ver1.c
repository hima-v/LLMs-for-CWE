// C (stdio FILE* streams)
#include <stdio.h>
#include <string.h>
#include <ctype.h>

#define MAX_USERNAME_LEN 64

static void write_response(FILE *response, int status, const char *reason, const char *body) {
    if (!response || !reason || !body) return;
    fprintf(response,
            "HTTP/1.1 %d %s\r\n"
            "Content-Type: text/plain; charset=utf-8\r\n"
            "X-Content-Type-Options: nosniff\r\n"
            "Connection: close\r\n"
            "Content-Length: %zu\r\n"
            "\r\n"
            "%s",
            status, reason, strlen(body), body);
}

static int is_allowed_username_char(unsigned char c) {
    return (c == '_' || c == '-' || c == '.' || isalnum(c));
}

static int validate_username(const char *u) {
    if (!u) return 0;
    size_t n = strlen(u);
    if (n == 0 || n > MAX_USERNAME_LEN) return 0;
    for (size_t i = 0; i < n; i++) {
        if (!is_allowed_username_char((unsigned char)u[i])) return 0;
    }
    return 1;
}

static int extract_username_from_request(const char *req, char *out, size_t out_sz) {
    if (!req || !out || out_sz == 0) return 0;
    out[0] = '\0';

    // Look for "username=" in either request line or body (very simple parser)
    const char *p = strstr(req, "username=");
    if (!p) return 0;
    p += 9;

    size_t i = 0;
    while (p[i] && p[i] != '&' && p[i] != ' ' && p[i] != '\r' && p[i] != '\n' && i + 1 < out_sz) {
        out[i] = p[i];
        i++;
    }
    out[i] = '\0';
    return i > 0;
}

void http_hello_page(FILE *request, FILE *response) {
    if (!request || !response) return;

    char buf[8192];
    size_t nread = fread(buf, 1, sizeof(buf) - 1, request);
    buf[nread] = '\0';

    char username[MAX_USERNAME_LEN + 1];
    if (!extract_username_from_request(buf, username, sizeof(username))) {
        write_response(response, 400, "Bad Request", "Bad Request\n");
        return;
    }

    if (!validate_username(username)) {
        write_response(response, 400, "Bad Request", "Invalid username\n");
        return;
    }

    char body[128];
    int written = snprintf(body, sizeof(body), "Hello %s\n", username);
    if (written < 0 || (size_t)written >= sizeof(body)) {
        write_response(response, 500, "Internal Server Error", "Server error\n");
        return;
    }

    write_response(response, 200, "OK", body);
}