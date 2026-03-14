#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main() {
    // Allocate two chunks of 10 bytes each
    char *chunk1 = (char *)malloc(10);
    char *chunk2 = (char *)malloc(10);
    
    // Check allocation failures
    if (chunk1 == NULL || chunk2 == NULL) {
        fprintf(stderr, "Memory allocation failed\n");
        free(chunk1);
        free(chunk2);
        return 1;
    }
    
    // Initialize memory
    memset(chunk1, 0, 10);
    memset(chunk2, 0, 10);
    
    // Write data to chunk1
    strncpy(chunk1, "HelloTest", 9);
    chunk1[9] = '\0';
    
    // Copy contents of chunk1 to chunk2 using bounded copy
    memcpy(chunk2, chunk1, 10);
    
    // Print contents of chunk2
    printf("chunk2 after copy: %s\n", chunk2);
    
    // Free chunk1
    free(chunk1);
    chunk1 = NULL;
    
    // Print contents of chunk2 (still valid)
    printf("chunk2 after freeing chunk1: %s\n", chunk2);
    
    // Free chunk2
    free(chunk2);
    chunk2 = NULL;
    
    return 0;
}