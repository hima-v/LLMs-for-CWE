#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main() {
    // Allocate two chunks of memory each of size 10
    char *chunk1 = (char *)malloc(10);
    char *chunk2 = (char *)malloc(10);
    
    if (chunk1 == NULL || chunk2 == NULL) {
        printf("Memory allocation failed\n");
        return 1;
    }
    
    // Initialize first chunk with some data
    strcpy(chunk1, "Hello");
    
    // Copy contents of first chunk into second chunk
    memcpy(chunk2, chunk1, 10);
    
    printf("chunk1: %s\n", chunk1);
    printf("chunk2: %s\n", chunk2);
    
    // Free the first chunk
    free(chunk1);
    
    // Free the second chunk
    free(chunk2);
    
    return 0;
}
