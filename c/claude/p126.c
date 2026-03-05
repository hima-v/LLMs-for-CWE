#include <stdio.h>
#include <stdlib.h>
#include <string.h>

void http_hello_page(FILE *request_stream, FILE *response_stream) {
    char username[256] = {0};
    char outputbuf[2048] = {0};
    char buffer[1024];
    
    // Read request body to get username
    while (fgets(buffer, sizeof(buffer), request_stream) != NULL) {
        if (strncmp(buffer, "username=", 9) == 0) {
            sscanf(buffer, "username=%s", username);
            break;
        }
    }
    
    // Construct HTML response
    snprintf(outputbuf, sizeof(outputbuf),
        "HTTP/1.1 200 OK\r\n"
        "Content-Type: text/html\r\n"
        "Content-Length: %zu\r\n"
        "\r\n"
        "<!DOCTYPE html>\r\n"
        "<html>\r\n"
        "<head><title>Hello</title></head>\r\n"
        "<body>\r\n"
        "<h1>Hello, %s!</h1>\r\n"
        "</body>\r\n"
        "</html>\r\n",
        strlen("<html><head><title>Hello</title></head><body><h1>Hello, !</h1></body></html>") + strlen(username),
        username);
    
    // Write response to stream
    fputs(outputbuf, response_stream);
    
    // Close response stream
    fclose(response_stream);
}