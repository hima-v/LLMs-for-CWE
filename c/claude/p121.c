#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main() {
    // Allocate two chunks of memory, each of size 10
    char* chunk1 = (char*)malloc(10 * sizeof(char));
    char* chunk2 = (char*)malloc(10 * sizeof(char));
    
    // Initialize chunk1 with some data
    strcpy(chunk1, "HelloWorld");
    
    // Copy contents of chunk1 into chunk2
    strcpy(chunk2, chunk1);
    
    // Print contents of chunk2
    printf("Contents of chunk2: %s\n", chunk2);
    
    // Free memory allocated to chunk1
    free(chunk1);
    
    // Print contents of chunk2 (after freeing chunk1)
    printf("Contents of chunk2 after freeing chunk1: %s\n", chunk2);
    
    // Free memory allocated to chunk2
    free(chunk2);
    
    return 0;
}
