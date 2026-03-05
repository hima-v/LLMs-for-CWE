#include <stdio.h>
#include <stdlib.h>
#include <string.h>

char* getQueryParam(const char* query, const char* param) {
    static char value[256];
    char search[256];
    sprintf(search, "%s=", param);
    
    const char* pos = strstr(query, search);
    if (pos) {
        pos += strlen(search);
        int i = 0;
        while (pos[i] && pos[i] != '&' && i < 255) {
            value[i] = pos[i];
            i++;
        }
        value[i] = '\0';
        return value;
    }
    return NULL;
}

int check_mod(const char* username, const char* password) {
    if (strcmp(username, "admin") == 0 && strcmp(password, "password123") == 0) {
        return 1;
    }
    return 0;
}

int main() {
    printf("Content-type: text/plain\r\n\r\n");
    
    const char* query_string = getenv("QUERY_STRING");
    if (!query_string) {
        printf("false");
        return 0;
    }
    
    char* username = getQueryParam(query_string, "username");
    char* password = getQueryParam(query_string, "password");
    
    if (username && password && check_mod(username, password)) {
        printf("true");
    } else {
        printf("false");
    }
    
    return 0;
}
