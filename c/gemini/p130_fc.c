#include <stdio.h>
#include <string.h>
#include <ctype.h>

// Fixed template buffer
const char *TEMPLATE = "<h1>Hello, %s!</h1>";

void render_response_from_env(const char *input_name, char *output) {
    char clean_name[21] = "Guest";
    
    // Strict Validation
    if (input_name != NULL && strlen(input_name) <= 20) {
        int i = 0;
        while (input_name[i] != '\0' && isalnum(input_name[i])) {
            clean_name[i] = input_name[i];
            i++;
        }
        clean_name[i] = '\0';
    }
    
    // Securely format (Note: In production, escape HTML entities here)
    sprintf(output, TEMPLATE, clean_name);
}

// In a CGI/Server context
void home(const char *raw_input) {
    char response[100];
    render_response_from_env(raw_input, response);
    printf("Content-Type: text/html\n\n%s", response);
}