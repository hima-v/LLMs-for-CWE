// C
#include <stdio.h>
#include <string.h>
#include <ctype.h>

void http_hello_page(FILE *request, FILE *response) {
    char username[51];
    if (fgets(username, sizeof(username), request) == NULL) return;
    
    size_t len = strlen(username);
    while (len > 0 && (username[len-1] == '\n' || username[len-1] == '\r')) username[--len] = '\0';

    int valid = (len > 0 && len <= 50);
    for (int i = 0; i < len; i++) {
        if (!isalnum(username[i])) valid = 0;
    }

    if (!valid) {
        fprintf(response, "HTTP/1.1 400 Bad Request\r\nContent-Type: text/plain; charset=utf-8\r\n\r\nInvalid Username");
    } else {
        fprintf(response, "HTTP/1.1 200 OK\r\nContent-Type: text/plain; charset=utf-8\r\n\r\nHello %s", username);
    }
}