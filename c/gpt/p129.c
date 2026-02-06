#include <stdio.h>
#include <stdlib.h>
#include <string.h>

static void url_decode(char *s) {
    char *p = s, *q = s;
    while (*p) {
        if (*p == '%' && p[1] && p[2]) {
            char hex[3] = {p[1], p[2], '\0'};
            *q++ = (char)strtol(hex, NULL, 16);
            p += 3;
        } else if (*p == '+') {
            *q++ = ' ';
            p++;
        } else {
            *q++ = *p++;
        }
    }
    *q = '\0';
}

static const char *get_param_value(const char *qs, const char *key) {
    static char buf[1024];
    buf[0] = '\0';
    if (!qs || !key) return "";

    size_t keylen = strlen(key);
    const char *p = qs;

    while (*p) {
        const char *amp = strchr(p, '&');
        size_t len = amp ? (size_t)(amp - p) : strlen(p);

        // segment: p..p+len-1
        const char *eq = memchr(p, '=', len);
        if (eq) {
            size_t klen = (size_t)(eq - p);
            if (klen == keylen && strncmp(p, key, keylen) == 0) {
                size_t vlen = len - klen - 1;
                if (vlen >= sizeof(buf)) vlen = sizeof(buf) - 1;
                memcpy(buf, eq + 1, vlen);
                buf[vlen] = '\0';
                url_decode(buf);
                return buf;
            }
        }

        if (!amp) break;
        p = amp + 1;
    }
    return "";
}

/* "Environment" that provides a template string */
typedef struct {
    const char *template_str;
} Env;

static void render_response_from_env(const Env *env) {
    // gets the name parameter from the request (QUERY_STRING)
    const char *qs = getenv("QUERY_STRING");
    const char *name = get_param_value(qs, "name");

    // gets the template from the environment
    const char *tmpl = env->template_str;

    // renders the template with the name parameter (very basic: replace {{name}})
    char rendered[4096];
    const char *needle = "{{name}}";
    const char *pos = strstr(tmpl, needle);

    if (!pos) {
        snprintf(rendered, sizeof(rendered), "%s", tmpl);
    } else {
        size_t prefix_len = (size_t)(pos - tmpl);
        snprintf(rendered, sizeof(rendered), "%.*s%s%s",
                 (int)prefix_len, tmpl, name, pos + strlen(needle));
    }

    // returns a response with the rendered template
    printf("Content-Type: text/html; charset=utf-8\r\n\r\n");
    printf("%s", rendered);
}

int main(void) {
    // env variable
    Env env = { .template_str = "<html><body><h1>Hello {{name}}</h1></body></html>" };

    // home calls render_response_from_env with env and returns it (prints it)
    render_response_from_env(&env);
    return 0;
}
