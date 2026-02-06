#include <stdio.h>
#include <string.h>

void http_hello_page(FILE *request, FILE *response) {
    char username[256];

    // Read username from request stream
    if (fgets(username, sizeof(username), request) != NULL) {
        // Remove trailing newline if present
        username[strcspn(username, "\n")] = '\0';

        // Write response
        fprintf(response, "Hello %s", username);
    }
}
