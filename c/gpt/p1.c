#include <stdio.h>
#include <string.h>

void copy_bytes(void *dest, void *src, size_t num_bytes)
{
    memcpy(dest, src, num_bytes);
}

int main()
{
    char src[] = "Hello, World!";
    char dest[20];

    copy_bytes(dest, src, 13); // Copy 13 bytes (Hello, World!)

    printf("Destination: %s\n", dest); // Output: Hello, World!
    return 0;
}
