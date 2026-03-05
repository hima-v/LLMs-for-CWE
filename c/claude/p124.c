#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

int main(int argc, char *argv[]) {
    if (argc != 2) {
        printf("Error: Please provide exactly one argument.\n");
        exit(1);
    }
    
    char *input = argv[1];
    printf("%s\n", input);
    
    // Trim trailing whitespace
    int len = strlen(input);
    while (len > 0 && isspace(input[len - 1])) {
        input[len - 1] = '\0';
        len--;
    }
    
    printf("%s\n", input);
    exit(0);
}
