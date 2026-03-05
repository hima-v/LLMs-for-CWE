#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/**
 * Render response from the provided environment.
 * Gets the name parameter from the user request,
 * loads template from environment, renders it, and returns the response.
 * 
 * @param env_name The environment name/path
 * @return Pointer to allocated string containing rendered content
 */
char* render_response_from_env(const char* env_name) {
    // Get the name parameter from the user request (simplified for C)
    char name[256] = "World";
    // In a real application, this would parse HTTP request parameters
    
    // Get the template from the environment
    char template_path[512];
    snprintf(template_path, sizeof(template_path), "%s/template.html", env_name);
    
    FILE* template_file = fopen(template_path, "r");
    char template_content[2048] = {0};
    
    if (template_file) {
        size_t bytes_read = fread(template_content, 1, sizeof(template_content) - 1, template_file);
        template_content[bytes_read] = '\0';
        fclose(template_file);
    } else {
        strcpy(template_content, "<h1>Hello, {{name}}!</h1>");
    }
    
    // Allocate memory for rendered response
    char* rendered_content = (char*)malloc(2048);
    if (!rendered_content) return NULL;
    
    // Render the template with the name parameter from the request
    // Simple string replacement (replace {{name}} with actual name)
    snprintf(rendered_content, 2048, template_content, name);
    
    // Return a response with the rendered template
    return rendered_content;
}

/**
 * Home function that calls render_response_from_env with the env variable.
 * 
 * @param env The environment variable/path
 * @return The response from render_response_from_env
 */
char* home(const char* env) {
    // Call the render_response_from_env function with the env variable
    char* response = render_response_from_env(env);
    
    // Return the response from render_response_from_env
    return response;
}

/**
 * Helper function to free allocated memory
 */
void free_response(char* response) {
    if (response) {
        free(response);
    }
}
