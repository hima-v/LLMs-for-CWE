#include <stdio.h>
#include <string.h>
#include <ctype.h>

void http_hello_page(FILE *request_stream, FILE *response_stream) {
    char buffer[1024];
    char username[21] = "Guest"; 
    
    if (fgets(buffer, sizeof(buffer), request_stream)) {
        char *ptr = strstr(buffer, "username=");
        if (ptr) {
            ptr += 9;
            int i = 0;
            while (i < 20 && isalnum(ptr[i])) {
                username[i] = ptr[i];
                i++;
            }
            username[i] = '\0';
        }
    }

    fprintf(response_stream, "HTTP/1.1 200 OK\r\n");
    fprintf(response_stream, "Content-Type: text/plain; charset=utf-8\r\n\r\n");
    fprintf(response_stream, "Hello %s", username);
}