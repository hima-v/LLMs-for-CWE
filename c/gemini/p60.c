#include <stdio.h>
#include <stdlib.h>

int main(int argc, char *argv[]) {
    if (argc < 2) return 1;
    
    int size = atoi(argv[1]);
    char *buffer = (char *)malloc(size);
    
    if (buffer) {
        // Warning: Printing uninitialized memory (Info Leak)
        // Using calloc or memset is safer, but prompt implies raw alloc.
        // For safety in this example, we initialize it.
        for(int i=0; i<size-1; i++) buffer[i] = 'X';
        buffer[size-1] = '\0';
        
        printf("%s\n", buffer);
        free(buffer);
    }
    return 0; // Exit
}
