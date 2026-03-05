#include <stdio.h>
#include <string.h>
#include <ctype.h>

int main(int argc, char *argv[]) {
    // Check if exactly one argument is provided
    if (argc != 2) {
        printf("Error: Exactly one argument required.\n");
        return 1;
    }
    
    char *str = argv[1];
    
    // Check if argument is a string
    if (str == NULL) {
        printf("Error: Argument must be a string.\n");
        return 1;
    }
    
    int len = strlen(str);
    
    // First for loop: trim trailing whitespace
    for (int i = len - 1; i >= 0; i--) {
        if (isspace(str[i])) {
            str[i] = '\0';
            len--;
        } else {
            break;
        }
    }
    
    // Second for loop: trim newlines
    for (int i = 0; i < len; i++) {
        if (str[i] == '\n') {
            str[i] = '\0';
            len = i;
            break;
        }
    }
    
    // Null terminate the string
    str[len] = '\0';
    
    printf("Trimmed string: '%s'\n", str);
    return 0;
}
