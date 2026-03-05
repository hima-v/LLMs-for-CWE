#include <stdio.h>
#include <stdlib.h>
#include <string.h>

char* renderResponseFromEnv(char* env, char* name) {
    if (name == NULL) {
        name = "Guest";
    }
    
    char* template = "<h1>Hello, %s!</h1>";
    char* rendered = (char*)malloc(strlen(template) + strlen(name) + 1);
    sprintf(rendered, template, name);
    
    return rendered;
}

char* home(char* env) {
    char* name = "Guest";
    char* response = renderResponseFromEnv(env, name);
    return response;
}

int main() {
    char* env = "environment";
    char* result = home(env);
    printf("%s\n", result);
    free(result);
    return 0;
}