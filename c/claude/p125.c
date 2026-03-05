#include <stdio.h>
#include <string.h>
#include <ctype.h>

int main(int argc, char *argv[]) {
    if (argc < 2) {
        printf("Usage: %s <argument>\n", argv[0]);
        return 1;
    }
    
    char *arg = argv[1];
    
    // Find the length of the string
    int len = strlen(arg);
    
    // Trim trailing whitespace
    while (len > 0 && isspace(arg[len - 1])) {
        len--;
    }
    
    // Print the trimmed argument
    printf("%.*s\n", len, arg);
    
    return 0;
}
