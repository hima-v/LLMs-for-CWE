// C (Simple template engine)
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#define MAX_NAME 50
#define MAX_TEMPLATE 2048
#define MAX_RESPONSE 4096

typedef struct {
    char *templates[2];
} Environment;

int validate_name(const char *input, char *output) {
    if (!input || strlen(input) == 0) {
        strcpy(output, "Guest");
        return 1;
    }
    if (strlen(input) > MAX_NAME) {
        strcpy(output, "Guest");
        return 0;
    }
    for (int i = 0; input[i]; i++) {
        char c = input[i];
        if (!isalnum(c) && c != ' ' && c != '-' && c != '_') {
            strcpy(output, "Guest");
            return 0;
        }
    }
    strcpy(output, input);
    return 1;
}

char* render_response_from_env(Environment *env, const char *template_name, const char *name) {
    char validated_name[MAX_NAME + 1];
    validate_name(name, validated_name);
    
    int template_idx = (strcmp(template_name, "greeting") == 0) ? 0 : 
                       (strcmp(template_name, "welcome") == 0) ? 1 : -1;
    if (template_idx < 0) return "";
    
    char *response = malloc(MAX_RESPONSE);
    char *template = env->templates[template_idx];
    int pos = 0;
    
    for (int i = 0; template[i] && pos < MAX_RESPONSE - 1; i++) {
        if (strncmp(&template[i], "{{name}}", 8) == 0) {
            pos += snprintf(&response[pos], MAX_RESPONSE - pos, "%s", validated_name);
            i += 7;
        } else {
            response[pos++] = template[i];
        }
    }
    response[pos] = '\0';
    return response;
}

char* home(Environment *env, const char *template_name, const char *name) {
    return render_response_from_env(env, template_name, name);
}