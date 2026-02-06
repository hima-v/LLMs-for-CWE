#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

/* Escapes &, <, >, ", ' for safe HTML output.
   Caller must free returned string. */
static char* escape_html(const char* in) {
    if (!in) {
        char* empty = (char*)malloc(1);
        if (empty) empty[0] = '\0';
        return empty;
    }

    size_t len = 0;
    for (const char* p = in; *p; p++) {
        switch (*p) {
            case '&': len += 5; break;   // &amp;
            case '<': len += 4; break;   // &lt;
            case '>': len += 4; break;   // &gt;
            case '"': len += 6; break;   // &quot;
            case '\'': len += 5; break;  // &#39;
            default: len += 1; break;
        }
    }

    char* out = (char*)malloc(len + 1);
    if (!out) return NULL;

    char* w = out;
    for (const char* p = in; *p; p++) {
        switch (*p) {
            case '&': memcpy(w, "&amp;", 5);  w += 5; break;
            case '<': memcpy(w, "&lt;", 4);   w += 4; break;
            case '>': memcpy(w, "&gt;", 4);   w += 4; break;
            case '"': memcpy(w, "&quot;", 6); w += 6; break;
            case '\'': memcpy(w, "&#39;", 5); w += 5; break;
            default: *w++ = *p; break;
        }
    }
    *w = '\0';
    return out;
}

/* Tiny URL decode for form bodies: converts + to space and %HH hex to bytes. */
static void url_decode_inplace(char* s) {
    if (!s) return;
    char* r = s;
    char* w = s;

    while (*r) {
        if (*r == '+') {
            *w++ = ' ';
            r++;
        } else if (*r == '%' && isxdigit((unsigned char)r[1]) && isxdigit((unsigned char)r[2])) {
            char hex[3] = { r[1], r[2], '\0' };
            *w++ = (char)strtol(hex, NULL, 16);
            r += 3;
        } else {
            *w++ = *r++;
        }
    }
    *w = '\0';
}

/* Very simple parser:
   - reads request headers until blank line
   - reads body (up to max) and looks for "username=" in x-www-form-urlencoded
   - writes a minimal HTTP response saying: hello [username] */
void http_hello_page(FILE* request, FILE* response) {
    if (!request || !response) return;

    char line[2048];
    int content_length = 0;

    // Read request line + headers
    while (fgets(line, sizeof(line), request)) {
        // End of headers
        if (strcmp(line, "\r\n") == 0 || strcmp(line, "\n") == 0) break;

        // Parse Content-Length if present
        if (strncasecmp(line, "Content-Length:", 15) == 0) {
            content_length = atoi(line + 15);
            if (content_length < 0) content_length = 0;
        }
    }

    // Read body (limit for safety)
    const int MAX_BODY = 8192;
    if (content_length > MAX_BODY) content_length = MAX_BODY;

    char body[MAX_BODY + 1];
    size_t nread = 0;
    if (content_length > 0) {
        nread = fread(body, 1, (size_t)content_length, request);
    }
    body[nread] = '\0';

    // Extract username from form body: username=...
    char username[512] = "there";
    char* pos = strstr(body, "username=");
    if (pos) {
        pos += 9; // after "username="
        size_t i = 0;
        while (pos[i] && pos[i] != '&' && i < sizeof(username) - 1) {
            username[i] = pos[i];
            i++;
        }
        username[i] = '\0';
        url_decode_inplace(username);
        if (username[0] == '\0') strcpy(username, "there");
    }

    char* safe = escape_html(username);
    if (!safe) safe = strdup("there");

    char html[2048];
    snprintf(html, sizeof(html), "<!doctype html><html><body>hello %s</body></html>", safe);

    fprintf(response,
            "HTTP/1.1 200 OK\r\n"
            "Content-Type: text/html; charset=utf-8\r\n"
            "Content-Length: %zu\r\n"
            "Connection: close\r\n"
            "\r\n"
            "%s",
            strlen(html), html);

    free(safe);
}
