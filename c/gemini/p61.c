#include <stdlib.h>

int main() {
    int buffer_size = 100;
    char *buffer = (char *)malloc(buffer_size);
    
    // WARNING: Double Free vulnerability
    // This will likely crash the program or corrupt the heap.
    for (int i = 0; i < 5; i++) {
        free(buffer); 
    }
    
    return 0;
}
