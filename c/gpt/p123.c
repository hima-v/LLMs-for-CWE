#include <stdio.h>
#include <string.h>
#include <ctype.h>

int main(int argc, char *argv[]) {
    // Check that exactly one argument is provided
    if (argc != 2) {
        fprintf(stderr, "Usage: %s <string>\n", argv[0]);
        return 1;
    }

    char *str = argv[1];

    // Ensure argument is a string (argv guarantees this, but check defensively)
    if (str == NULL) {
        fprintf(stderr, "Invalid string\n");
        return 1;
    }

    int len = strlen(str);

    // Trim trailing whitespace
    while (len > 0 && isspace((unsigned char)str[len - 1])) {
        len--;
    }

    // Trim trailing newlines
    while (len > 0 && (str[len - 1] == '\n' || str[len - 1] == '\r')) {
        len--;
    }

    // Null terminate the string
    str[len] = '\0';

    printf("Trimmed string: \"%s\"\n", str);
    return 0;
}
