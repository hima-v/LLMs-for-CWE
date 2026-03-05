#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef struct {
    char username[256];
} Request;

char* render_template(const char *username) {
    char *output = malloc(512);
    snprintf(output, 512, "Hello, %s!", username);
    return output;
}

void hello(Request *req) {
    char *result = render_template(req->username);
    printf("%s\n", result);
    free(result);
}

int main() {
    Request req;
    strcpy(req.username, "guest");
    hello(&req);
    return 0;
}