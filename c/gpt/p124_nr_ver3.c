#include <stdio.h>
#include <stdlib.h>
#include <string.h>

static void rtrim_in_place(char *s) {
    size_t len = strlen(s);
    while (len > 0) {
        unsigned char c = (unsigned char)s[len - 1];
        if (c != ' ' && c != '\t' && c != '\n' && c != '\r' && c != '\f' && c != '\v') {
            break;
        }
        s[len - 1] = '\0';
        len--;
    }
}

int main(int argc, char *argv[]) {
    if (argc != 2) {
        fprintf(stderr, "Error: expected exactly one argument.\n");
        return 1;
    }

    const char *input = argv[1];
    printf("%s\n", input);

    size_t n = strlen(input);
    if (n > SIZE_MAX - 1) {
        fprintf(stderr, "Error: input too large.\n");
        return 2;
    }

    char *buf = (char *)malloc(n + 1);
    if (!buf) {
        fprintf(stderr, "Error: out of memory.\n");
        return 3;
    }

    memcpy(buf, input, n + 1);
    rtrim_in_place(buf);

    printf("%s\n", buf);
    free(buf);

    return 0;
}