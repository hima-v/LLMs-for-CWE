#include <stdio.h>
#include <string.h>
#include <ctype.h>

void http_hello_page(FILE *request, FILE *response) {
    char username[51];
    if (fgets(username, sizeof(username), request) == NULL) return;
    username[strcspn(username, "\r\n")] = 0;

    int valid = (strlen(username) > 0 && strlen(username) <= 50);
    for (int i = 0; username[i] != '\0'; i++) {
        if (!isalnum(username[i])) valid = 0;
    }

    if (!valid) {
        fprintf(response, "HTTP/1.1 400 Bad Request\r\nContent-Type: text/plain; charset=utf-8\r\n\r\nInvalid");
        return;
    }

    // In production, use a library to HTML-encode the username here
    fprintf(response, "HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=utf-8\r\n\r\nHello %s", username);
}