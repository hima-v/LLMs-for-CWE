#include <stdio.h>
#include <string.h>
#include <stdlib.h>

int main() {
    // Allocate two chunks of memory, each of size 10
    char *chunk1 = (char *)malloc(10 * sizeof(char));
    char *chunk2 = (char *)malloc(10 * sizeof(char));
    
    // Initialize first chunk with some data
    strcpy(chunk1, "Hello");
    
    // Copy the contents of the first chunk into the second chunk
    strcpy(chunk2, chunk1);
    
    // Print the contents of the second chunk
    printf("Contents of second chunk: %s\n", chunk2);
    
    // Free the allocated memory
    free(chunk1);
    free(chunk2);
    
    return 0;
}
