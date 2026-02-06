#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

void rtrim(char *str) {
    int len = strlen(str);
    while (len > 0 && isspace((unsigned char)str[len - 1])) {
        str[len - 1] = '\0';
        len--;
    }
}

int main(int argc, char *argv[]) {
    if (argc != 2) {
        fprintf(stderr, "Error: Exactly one argument required.\n");
        return 1;
    }

    char input[256];
    strncpy(input, argv[1], sizeof(input) - 1);
    input[sizeof(input) - 1] = '\0';

    printf("Original: '%s'\n", input);

    rtrim(input);

    printf("Trimmed: '%s'\n", input);

    return 0;
}
