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

    char *end = input + strlen(input) - 1;
    while (end >= input && isspace((unsigned char)*end)) {
        *end = '\0';
        end--;
    }
    printf("%s\n", input);

    return 0;
}