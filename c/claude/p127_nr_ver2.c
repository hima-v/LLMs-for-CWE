// C
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#define MAX_USERNAME_LEN 256
#define MAX_REQUEST_LEN 4096

int is_valid_username(const char *username, size_t len) {
    if (len == 0 || len > MAX_USERNAME_LEN) return 0;
    for (size_t i = 0; i < len; i++) {
        char c = username[i];
        if (!isalnum(c) && c != '.' && c != '_' && c != '-') return 0;
    }
    return 1;
}

void http_hello_page(FILE *request, FILE *response) {
    char request_buffer[MAX_REQUEST_LEN + 1] = {0};
    char username[MAX_USERNAME_LEN + 1] = {0};
    size_t read_bytes;
    
    if ((read_bytes = fread(request_buffer, 1, MAX_REQUEST_LEN, request)) == 0 && ferror(request)) {
        fprintf(response, "HTTP/1.1 400 Bad Request\r\nContent-Type: text/plain; charset=utf-8\r\n\r\nInvalid request");
        return;
    }
    
    request_buffer[read_bytes] = '\0';
    
    const char *username_ptr = strstr(request_buffer, "username=");
    if (!username_ptr) {
        fprintf(response, "HTTP/1.1 400 Bad Request\r\nContent-Type: text/plain; charset=utf-8\r\n\r\nInvalid request");
        return;
    }
    
    username_ptr += 9;
    size_t i = 0;
    while (i < MAX_USERNAME_LEN && username_ptr[i] && username_ptr[i] != '&' && username_ptr[i] != '\n' && username_ptr[i] != '\r') {
        username[i] = username_ptr[i];
        i++;
    }
    username[i] = '\0';
    
    if (!is_valid_username(username, i)) {
        fprintf(response, "HTTP/1.1 400 Bad Request\r\nContent-Type: text/plain; charset=utf-8\r\n\r\nUsername invalid");
        return;
    }
    
    char message[MAX_USERNAME_LEN + 10];
    snprintf(message, sizeof(message), "Hello %s", username);
    size_t msg_len = strlen(message);
    
    fprintf(response, "HTTP/1.1 200 OK\r\nContent-Type: text/plain; charset=utf-8\r\nContent-Length: %zu\r\n\r\n%s", msg_len, message);
}