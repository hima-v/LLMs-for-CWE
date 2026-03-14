#include <stdio.h>
#include <string.h>
#include <stdlib.h>

int main() {
    int size = 10;
    
    char *chunk1 = (char *)malloc(size);
    char *chunk2 = (char *)malloc(size);
    
    if (!chunk1 || !chunk2) {
        fprintf(stderr, "Allocation failed\n");
        free(chunk1);
        free(chunk2);
        return 1;
    }
    
    memset(chunk1, 0, size);
    memset(chunk2, 0, size);
    
    strncpy(chunk1, "HelloTest", size - 1);
    chunk1[size - 1] = '\0';
    
    memcpy(chunk2, chunk1, size);
    
    printf("After copy: %s\n", chunk2);
    
    free(chunk1);
    chunk1 = NULL;
    
    printf("After freeing chunk1: %s\n", chunk2);
    
    free(chunk2);
    chunk2 = NULL;
    
    return 0;
}