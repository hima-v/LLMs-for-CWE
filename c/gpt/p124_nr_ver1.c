/* C */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

static void rtrim_inplace(char *s) {
    size_t len = strlen(s);
    while (len > 0 && isspace((unsigned char)s[len - 1])) {
        len--;
    }
    s[len] = '\0';
}

int main(int argc, char *argv[]) {
    if (argc != 2) {
        fprintf(stderr, "Error: expected exactly one argument.\n");
        return 2;
    }

    const char *arg = argv[1];
    printf("%s\n", arg);

    size_t n = strlen(arg);
    char *input = (char *)malloc(n + 1);
    if (!input) {
        fprintf(stderr, "Error: out of memory.\n");
        return 1;
    }
    memcpy(input, arg, n + 1);

    rtrim_inplace(input);
    printf("%s\n", input);

    free(input);
    return 0;
}