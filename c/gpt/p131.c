#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef struct {
    const char *template_str;  // "environment" holds template
} Env;

// Very small helper: extract name=... from a query string like "name=Bob&x=1"
static void get_query_param_name(const char *query, char *out, size_t out_sz) {
    const char *key = "name=";
    const char *p = strstr(query, key);
    if (!p) {
        snprintf(out, out_sz, "World");
        return;
    }
    p += strlen(key);
    size_t i = 0;
    while (p[i] && p[i] != '&' && i + 1 < out_sz) {
        out[i] = p[i];
        i++;
    }
    out[i] = '\0';
    if (out[0] == '\0') snprintf(out, out_sz, "World");
}

// Render a tiny template: replaces "{{name}}" with name
static void render_template(const char *tmpl, const char *name, char *out, size_t out_sz) {
    const char *needle = "{{name}}";
    const char *pos = strstr(tmpl, needle);
    if (!pos) {
        snprintf(out, out_sz, "%s", tmpl);
        return;
    }
    size_t prefix_len = (size_t)(pos - tmpl);
    snprintf(out, out_sz, "%.*s%s%s",
             (int)prefix_len, tmpl,
             name,
             pos + strlen(needle));
}

static void render_response_from_env(const Env *environment, const char *request_target, FILE *response) {
    // request_target like "/?name=Bob"
    const char *qmark = strchr(request_target, '?');
    const char *query = qmark ? (qmark + 1) : "";

    // get the name parameter from the user request
    char name[256];
    get_query_param_name(query, name, sizeof(name));

    // get the template from the environment
    const char *tmpl = environment->template_str;

    // render the template with the name parameter
    char body[1024];
    render_template(tmpl, name, body, sizeof(body));

    // return a response with the rendered template
    fprintf(response,
            "HTTP/1.1 200 OK\r\n"
            "Content-Type: text/html; charset=utf-8\r\n"
            "Content-Length: %zu\r\n"
            "\r\n"
            "%s",
            strlen(body), body);
}

static void home(const Env *env, const char *request_target, FILE *response) {
    // home calls render_response_from_env with env
    render_response_from_env(env, request_target, response);
}

// Demo "server": reads a request target from stdin and writes HTTP response to stdout
int main(void) {
    Env env = { .template_str = "Hello, {{name}}!" };

    // Example: user inputs "/?name=Suguna"
    char request_target[1024];
    if (!fgets(request_target, sizeof(request_target), stdin)) return 1;
    request_target[strcspn(request_target, "\r\n")] = 0;

    home(&env, request_target, stdout);
    return 0;
}
