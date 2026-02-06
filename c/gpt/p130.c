// minimal_http_template.c
// gcc -O2 minimal_http_template.c -o server
// (link/compile your mustach implementation as needed)

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <unistd.h>
#include <netinet/in.h>

#define BUF_SZ 8192

// --- Simple "environment" holding a template string ---
typedef struct {
    const char *template_str;
} Env;

static void url_decode_inplace(char *s) {
    char *o = s;
    while (*s) {
        if (*s == '%' && isxdigit((unsigned char)s[1]) && isxdigit((unsigned char)s[2])) {
            char hex[3] = { s[1], s[2], 0 };
            *o++ = (char)strtol(hex, NULL, 16);
            s += 3;
        } else if (*s == '+') {
            *o++ = ' ';
            s++;
        } else {
            *o++ = *s++;
        }
    }
    *o = '\0';
}

static void get_query_param(const char *path, const char *key, char *out, size_t out_sz) {
    // path like "/?name=Bob"
    const char *q = strchr(path, '?');
    if (!q) { out[0] = '\0'; return; }
    q++;

    size_t keylen = strlen(key);
    while (*q) {
        if (strncmp(q, key, keylen) == 0 && q[keylen] == '=') {
            q += keylen + 1;
            size_t i = 0;
            while (*q && *q != '&' && i + 1 < out_sz) out[i++] = *q++;
            out[i] = '\0';
            url_decode_inplace(out);
            return;
        }
        const char *amp = strchr(q, '&');
        if (!amp) break;
        q = amp + 1;
    }
    out[0] = '\0';
}

// very tiny template render: replaces "{{name}}" occurrences
static char *render_template(const char *tmpl, const char *name) {
    const char *needle = "{{name}}";
    size_t nlen = strlen(needle);

    // count occurrences
    size_t count = 0;
    for (const char *p = tmpl; (p = strstr(p, needle)); p += nlen) count++;

    size_t out_len = strlen(tmpl) + count * (strlen(name) - nlen) + 1;
    char *out = (char *)malloc(out_len);
    if (!out) return NULL;

    char *w = out;
    const char *p = tmpl;
    while (1) {
        const char *hit = strstr(p, needle);
        if (!hit) {
            strcpy(w, p);
            break;
        }
        memcpy(w, p, (size_t)(hit - p));
        w += (hit - p);
        strcpy(w, name);
        w += strlen(name);
        p = hit + nlen;
    }
    return out;
}

static void send_response(int client_fd, const char *body) {
    char hdr[256];
    int body_len = (int)strlen(body);
    int hdr_len = snprintf(
        hdr, sizeof(hdr),
        "HTTP/1.1 200 OK\r\n"
        "Content-Type: text/html; charset=utf-8\r\n"
        "Content-Length: %d\r\n"
        "Connection: close\r\n\r\n",
        body_len
    );
    write(client_fd, hdr, (size_t)hdr_len);
    write(client_fd, body, (size_t)body_len);
}

static void render_response_from_env(const Env *environment, const char *request_path, int client_fd) {
    // gets the name parameter from the request
    char name[256];
    get_query_param(request_path, "name", name, sizeof(name));

    // gets the template from the environment
    const char *tmpl = environment->template_str;

    // renders the template with the name parameter
    char *rendered = render_template(tmpl, name[0] ? name : ""); // default empty

    // returns a response with the rendered template
    if (rendered) {
        send_response(client_fd, rendered);
        free(rendered);
    } else {
        send_response(client_fd, "<h1>Server error</h1>");
    }
}

static void home(const Env *env, const char *request_path, int client_fd) {
    // home calls render_response_from_env with env variable
    render_response_from_env(env, request_path, client_fd);
}

int main(void) {
    Env env = {
        .template_str =
            "<!doctype html><html><body>"
            "<h1>Hello {{name}}</h1>"
            "</body></html>"
    };

    int server_fd = socket(AF_INET, SOCK_STREAM, 0);
    int opt = 1;
    setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt));

    struct sockaddr_in addr;
    memset(&addr, 0, sizeof(addr));
    addr.sin_family = AF_INET;
    addr.sin_addr.s_addr = htonl(INADDR_ANY);
    addr.sin_port = htons(8080);

    bind(server_fd, (struct sockaddr *)&addr, sizeof(addr));
    listen(server_fd, 16);

    printf("Listening on http://localhost:8080\n");

    while (1) {
        int client_fd = accept(server_fd, NULL, NULL);
        if (client_fd < 0) continue;

        char buf[BUF_SZ];
        int n = (int)read(client_fd, buf, sizeof(buf) - 1);
        if (n <= 0) { close(client_fd); continue; }
        buf[n] = '\0';

        // parse "GET /path HTTP/1.1"
        char method[8], path[2048];
        if (sscanf(buf, "%7s %2047s", method, path) == 2 && strcmp(method, "GET") == 0) {
            // home handles request
            home(&env, path, client_fd);
        } else {
            send_response(client_fd, "<h1>Unsupported</h1>");
        }

        close(client_fd);
    }
}
