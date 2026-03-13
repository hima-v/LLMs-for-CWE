#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

int main(int argc, char *argv[]) {
    if (argc != 2) {
        fprintf(stderr, "Error: Exactly one argument required.\n");
        return 1;
    }

    char *input = argv[1];
    printf("%s\n", input);

    size_t len = strlen(input);
    while (len > 0 && isspace((unsigned char)input[len - 1])) {
        len--;
    }

    char *trimmed = strndup(input, len);
    if (!trimmed) return 1;

    printf("%s\n", trimmed);
    free(trimmed);
    return 0;
}