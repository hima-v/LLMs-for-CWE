#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main() {
    char *query = getenv("QUERY_STRING");

    printf("Content-Type: text/plain\n\n");

    if (query && strstr(query, "username=admin") && strstr(query, "password=secret")) {
        printf("true");
    } else {
        printf("false");
    }

    return 0;
}
