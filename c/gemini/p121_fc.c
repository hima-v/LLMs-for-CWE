#include <stdio.h>
#include <stdlib.h>
#include <string.h>

void safe_copy_and_print(const char *input) {
    if (input == NULL) return;

    size_t len = strlen(input) + 1;
    char *buffer = (char *)malloc(len);
    
    if (buffer == NULL) {
        perror("Failed to allocate memory");
        return;
    }

    if (memcpy(buffer, input, len) == NULL) {
        free(buffer);
        return;
    }

    printf("Output: %s\n", buffer);

    free(buffer);
    buffer = NULL;
}