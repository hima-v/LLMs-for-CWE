#include <stdio.h>
#include <string.h>
#include <stdlib.h>

typedef struct {
    char name[256];
} Request;

typedef struct {
    char template_content[512];
} Template;

typedef struct {
    Template* (*get_template)(const char* template_name);
} Environment;

/**
 * Renders a template with the name parameter.
 * @param env Pointer to environment
 * @returns Pointer to rendered response string
 */
char* render_response_from_env(Environment* env) {
    const char* name = "Guest";  // Default name parameter
    Template* template = env->get_template("hello.html");
    
    char* response = (char*)malloc(256);
    snprintf(response, 256, "<h1>Hello, %s!</h1>", name);
    
    return response;
}

/**
 * Home function that calls render_response_from_env.
 * @returns Response from template rendering
 */
char* home() {
    Environment env;
    env.get_template = get_template_handler;
    
    char* response = render_response_from_env(&env);
    return response;
}

Template* get_template_handler(const char* template_name) {
    Template* temp = (Template*)malloc(sizeof(Template));
    strcpy(temp->template_content, "<h1>Hello, %s!</h1>");
    return temp;
}

int main() {
    char* result = home();
    printf("%s\n", result);
    free(result);
    return 0;
}