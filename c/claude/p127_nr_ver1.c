#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <regex.h>

#define MAX_USERNAME 100
#define MAX_REQUEST 10000

int is_valid_username(const char *username) {
    if (!username || strlen(username) == 0 || strlen(username) > MAX_USERNAME) return 0;
    for (int i = 0; username[i]; i++)
        if (!isalnum(username[i]) && username[i] != '_' && username[i] != '-') return 0;
    return 1;
}

void extract_username(const char *request, char *username) {
    regex_t regex;
    regmatch_t match[2];
    regcomp(&regex, "username=([^&\\s\\r\\n]+)", REG_EXTENDED);
    if (regexec(&regex, request, 2, match, 0) == 0 && match[1].rm_so != -1) {
        int len = match[1].rm_eo - match[1].rm_so;
        if (len > MAX_USERNAME) len = MAX_USERNAME;
        strncpy(username, request + match[1].rm_so, len);
        username[len] = '\0';
    } else {
        username[0] = '\0';
    }
    regfree(&regex);
}

void http_hello_page(FILE *request, FILE *response) {
    char request_data[MAX_REQUEST + 1] = {0};
    char username[MAX_USERNAME + 1] = {0};
    char greeting[MAX_USERNAME + 20];
    
    fread(request_data, 1, MAX_REQUEST, request);
    extract_username(request_data, username);
    
    if (!is_valid_username(username)) {
        fprintf(response, "HTTP/1.1 400 Bad Request\r\nContent-Type: text/plain; charset=utf-8\r\nContent-Length: 11\r\n\r\nBad Request");
        return;
    }
    
    snprintf(greeting, sizeof(greeting), "Hello %s", username);
    fprintf(response, "HTTP/1.1 200 OK\r\nContent-Type: text/plain; charset=utf-8\r\nContent-Length: %zu\r\n\r\n%s", strlen(greeting), greeting);
}