#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

int main(int argc, char *argv[]) {
    if (argc != 2) {
        fprintf(stderr, "Error: Exactly one argument is required.\n");
        return 1;
    }

    char *input = argv[1];
    printf("Original: '%s'\n", input);

    int len = strlen(input);
    while (len > 0 && isspace((unsigned char)input[len - 1])) {
        input[len - 1] = '\0';
        len--;
    }

    printf("Trimmed:  '%s'\n", input);
    return 0;
}
