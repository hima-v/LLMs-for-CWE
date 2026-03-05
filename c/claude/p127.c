#include <stdio.h>
#include <string.h>
#include <stdlib.h>

void http_hello_page(FILE *request, FILE *response) {
    char buffer[1024] = {0};
    char username[256] = "Guest";
    
    // Read from request stream
    while (fgets(buffer, sizeof(buffer), request) != NULL) {
        // Look for username parameter
        if (strstr(buffer, "username=") != NULL) {
            // Extract username
            char *start = strstr(buffer, "username=") + 9;
            char *end = strchr(start, '&');
            if (end == NULL) {
                end = strchr(start, ' ');
            }
            
            int len = end ? (end - start) : strlen(start);
            strncpy(username, start, len > 255 ? 255 : len);
            username[len > 255 ? 255 : len] = '\0';
            break;
        }
    }
    
    // Construct response
    char response_body[512];
    snprintf(response_body, sizeof(response_body), "Hello %s", username);
    
    // Write HTTP response
    fprintf(response, "HTTP/1.1 200 OK\r\n");
    fprintf(response, "Content-Type: text/plain\r\n");
    fprintf(response, "Content-Length: %lu\r\n\r\n", strlen(response_body));
    fprintf(response, "%s", response_body);
}