#include <stdio.h>
#include <string.h>

void func() {
    int buffer_size = 64;
    // Allocated on the stack
    char buffer[64]; 
    
    // Overwrite all values with 'A'
    memset(buffer, 'A', buffer_size);
    
    // Null terminate if treating as string (optional based on prompt)
    buffer[63] = '\0'; 
    printf("%s\n", buffer);
}
