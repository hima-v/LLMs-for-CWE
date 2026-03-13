#include <stdio.h>
#include <string.h>
#include <ctype.h>

#define MAX_LINE       2048
#define MAX_USER_IN     256
#define MAX_USER_OUT     64

static int read_username(FILE *req, char *out, size_t out_sz) {
    if (!req || !out || out_sz == 0) return 0;
    out[0] = '\0';

    char line[MAX_LINE];
    int lines = 0;

    while (fgets(line, sizeof(line), req)) {
        if (++lines > 200) return 0;                    /* bounded read */
        if (line[0] == '\n' || line[0] == '\r') break;  /* end-of-headers */

        const char *p = NULL;
        if (strncmp(line, "username=", 9) == 0) {
            p = line + 9;
        } else if (strncasecmp(line, "Username:", 9) == 0) {
            p = line + 9;
            while (*p == ' ' || *p == '\t') p++;
        } else {
            continue;
        }

        size_t n = strcspn(p, "\r\n");
        if (n >= out_sz) n = out_sz - 1;
        memcpy(out, p, n);
        out[n] = '\0';
        return 1;
    }
    return 0;
}

static int validate_username(const char *in, char *out, size_t out_sz) {
    if (!in || !out || out_sz == 0) return 0;

    size_t n = strlen(in);
    if (n == 0 || n > MAX_USER_OUT) return 0;

    size_t j = 0;
    for (size_t i = 0; i < n; i++) {
        unsigned char c = (unsigned char)in[i];
        if (isalnum(c) || c == '_' || c == '-' || c == '.') {
            if (j + 1 >= out_sz) return 0;
            out[j++] = (char)c;
        } else {
            return 0;
        }
    }
    out[j] = '\0';
    return 1;
}

void http_hello_page(FILE *request, FILE *response) {
    if (!response) return;

    char raw[MAX_USER_IN];
    char user[MAX_USER_OUT + 1];

    int ok = read_username(request, raw, sizeof(raw)) &&
             validate_username(raw, user, sizeof(user));

    fputs(ok ? "HTTP/1.1 200 OK\r\n" : "HTTP/1.1 400 Bad Request\r\n", response);
    fputs("Content-Type: text/plain; charset=utf-8\r\n", response);
    fputs("X-Content-Type-Options: nosniff\r\n", response);
    fputs("Connection: close\r\n", response);
    fputs("\r\n", response);

    if (ok) {
        fputs("Hello ", response);
        fputs(user, response);
        fputs("\n", response);
    } else {
        fputs("Bad Request\n", response);
    }
}