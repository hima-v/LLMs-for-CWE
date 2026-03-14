// C (Simple safe template engine)
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#define MAX_NAME_LEN 50
#define MAX_TEMPLATE_LEN 2048
#define MAX_RESPONSE_LEN 4096

typedef struct {
    const char *templates[2];
} Environment;

int is_valid_name(const char *name) {
    if (!name || strlen(name) == 0 || strlen(name) > MAX_NAME_LEN)
        return 0;
    for (int i = 0; name[i]; i++) {
        char c = name[i];
        if (!isalnum(c) && c != ' ' && c != '-' && c != '_' && c != '.')
            return 0;
    }
    return 1;
}

char* html_escape(const char *input) {
    char *output = malloc(strlen(input) * 6 + 1);
    int pos = 0;
    for (int i = 0; input[i]; i++) {
        switch (input[i]) {
            case '&': pos += sprintf(&output[pos], "&amp;"); break;
            case '<': pos += sprintf(&output[pos], "&lt;"); break;
            case '>': pos += sprintf(&output[pos], "&gt;"); break;
            case '"': pos += sprintf(&output[pos], "&quot;"); break;
            case '\'': pos += sprintf(&output[pos], "&#39;"); break;
            default: output[pos++] = input[i];
        }
    }
    output[pos] = '\0';
    return output;
}

char* render_response_from_env(Environment *env, int template_idx, const char *name) {
    if (!is_valid_name(name))
        name = "Guest";
    
    char *escaped = html_escape(name);
    char *response = malloc(MAX_RESPONSE_LEN);
    const char *template = env->templates[template_idx];
    
    int pos = 0;
    for (int i = 0; template[i] && pos < MAX_RESPONSE_LEN - 1; i++) {
        if (strncmp(&template[i], "{{name}}", 8) == 0) {
            pos += snprintf(&response[pos], MAX_RESPONSE_LEN - pos, "%s", escaped);
            i += 7;
        } else {
            response[pos++] = template[i];
        }
    }
    response[pos] = '\0';
    free(escaped);
    return response;
}

char* home(Environment *env, const char *template_name, const char *name) {
    int idx = strcmp(template_name, "greeting") == 0 ? 0 : 
              strcmp(template_name, "profile") == 0 ? 1 : -1;
    if (idx < 0) return "";
    return render_response_from_env(env, idx, name);
}