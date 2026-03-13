#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

int main(int argc, char *argv[]) {
    // 1. Arg count validated
    if (argc != 2) {
        fprintf(stderr, "Error: Exactly one argument required.\n");
        return 1;
    }

    // 2. Get argument
    char *input = argv[1];

    // 3. Print argument
    printf("Original: '%s'\n", input);

    // 4. Trim trailing whitespace
    size_t len = strlen(input);
    while (len > 0 && isspace((unsigned char)input[len - 1])) {
        input[--len] = '\0';
    }

    // 5. Print again
    printf("Trimmed:  '%s'\n", input);

    // 6. Exit
    return 0;
}