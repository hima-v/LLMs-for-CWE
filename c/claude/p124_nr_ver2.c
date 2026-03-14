#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

int main(int argc, char *argv[]) {
    if (argc != 2) {
        fprintf(stderr, "Error: Exactly one argument required\n");
        return 1;
    }
    
    char *input_var = argv[1];
    printf("%s\n", input_var);
    
    size_t len = strlen(input_var);
    while (len > 0 && isspace((unsigned char)input_var[len - 1])) {
        len--;
    }
    input_var[len] = '\0';
    printf("%s\n", input_var);
    
    return 0;
}