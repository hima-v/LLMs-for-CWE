#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#define MAX_NAME_LEN 50

static int is_allowed_char(unsigned char c) {
    return isalnum(c) || c == ' ' || c == '_' || c == '.' || c == '-';
}

static void safe_name(const char *in, char *out, size_t out_sz) {
    const char *fallback = "Guest";
    if (!in) {
        strncpy(out, fallback, out_sz - 1);
        out[out_sz - 1] = '\0';
        return;
    }

    // Trim leading/trailing spaces
    while (*in && isspace((unsigned char)*in)) in++;
    size_t len = strlen(in);
    while (len > 0 && isspace((unsigned char)in[len - 1])) len--;

    if (len == 0 || len > MAX_NAME_LEN) {
        strncpy(out, fallback, out_sz - 1);
        out[out_sz - 1] = '\0';
        return;
    }

    // Validate: starts with letter; only allowlisted chars
    if (!isalpha((unsigned char)in[0])) {
        strncpy(out, fallback, out_sz - 1);
        out[out_sz - 1] = '\0';
        return;
    }

    for (size_t i = 0; i < len; i++) {
        unsigned char c = (unsigned char)in[i];
        if (!is_allowed_char(c)) {
            strncpy(out, fallback, out_sz - 1);
            out[out_sz - 1] = '\0';
            return;
        }
    }

    size_t copy_len = (len < out_sz - 1) ? len : (out_sz - 1);
    memcpy(out, in, copy_len);
    out[copy_len] = '\0';
}

// Fixed/allowlisted template (not user-chosen)
static const char *TEMPLATE =
    "<!doctype html><html><head><meta charset=\"utf-8\"><title>Home</title></head>"
    "<body><h1>Hello, %s!</h1></body></html>";

static void html_escape(const char *in, char *out, size_t out_sz) {
    // Minimal HTML escaping to prevent injection in output context
    size_t o = 0;
    for (size_t i = 0; in[i] && o + 6 < out_sz; i++) {
        switch (in[i]) {
            case '&': memcpy(out + o, "&amp;", 5); o += 5; break;
            case '<': memcpy(out + o, "&lt;", 4); o += 4; break;
            case '>': memcpy(out + o, "&gt;", 4); o += 4; break;
            case '"': memcpy(out + o, "&quot;", 6); o += 6; break;
            case '\'': memcpy(out + o, "&#39;", 5); o += 5; break;
            default: out[o++] = in[i]; break;
        }
    }
    out[o] = '\0';
}

static void render_response_from_env(const char *query_name) {
    char name[MAX_NAME_LEN + 1];
    char escaped[(MAX_NAME_LEN * 6) + 1];

    safe_name(query_name, name, sizeof(name));
    html_escape(name, escaped, sizeof(escaped));

    printf("Content-Type: text/html; charset=utf-8\r\n\r\n");
    printf(TEMPLATE, escaped);
}

int main(void) {
    // Example "env": CGI QUERY_STRING parsing for "name=" parameter
    const char *qs = getenv("QUERY_STRING");
    const char *name_val = NULL;

    if (qs) {
        const char *p = strstr(qs, "name=");
        if (p) name_val = p + 5;
    }

    // home calls render_response_from_env with env/request-derived input
    render_response_from_env(name_val);
    return 0;
}