#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <ctype.h>

/* Function to escape special characters to HTML-safe sequences */
char* escape(const char* input) {
    if (input == NULL) return NULL;
    
    char* output = malloc(strlen(input) * 6 + 1); // Worst case: each char becomes 6 chars
    int j = 0;
    
    for (int i = 0; input[i] != '\0'; i++) {
        switch (input[i]) {
            case '&':
                strcpy(output + j, "&amp;");
                j += 5;
                break;
            case '<':
                strcpy(output + j, "&lt;");
                j += 4;
                break;
            case '>':
                strcpy(output + j, "&gt;");
                j += 4;
                break;
            case '"':
                strcpy(output + j, "&quot;");
                j += 6;
                break;
            case '\':
                strcpy(output + j, "&#039;");
                j += 6;
                break;
            default:
                output[j++] = input[i];
                break;
        }
    }
    output[j] = '\0';
    return output;
}

/* Decorator function that maps URL to handler and processes username parameter */
char* handleRequest(const char* url, const char* username) {
    char* result = malloc(256);
    
    /* Check if URL matches /hello/username pattern */
    if (strncmp(url, "/hello/", 7) == 0) {
        /* Escape the username to prevent HTML injection */
        char* escapedUsername = escape(username);
        sprintf(result, "Hello %s", escapedUsername);
        free(escapedUsername);
        return result;
    }
    
    sprintf(result, "404 Not Found");
    return result;
}

int main() {
    /* Simulate a request to /hello/<username> */
    const char* url = "/hello/john";
    const char* username = "<script>alert('xss')</script>";
    
    char* response = handleRequest(url, username);
    printf("Response: %s\n", response);
    
    free(response);
    return 0;
}
