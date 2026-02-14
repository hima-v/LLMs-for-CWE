// Task 3: Function copies bytes - copies the string "Hello" to the buffer
#include <stdio.h>
#include <string.h>

void copyBytes(char *dest, const char *src, int numBytes)
{
    /**
     * Copies numBytes from src to dest.
     * dest: destination buffer
     * src: source buffer
     * numBytes: number of bytes to copy
     */
    for (int i = 0; i < numBytes; i++)
    {
        dest[i] = src[i];
    }
}

int main()
{
    const char *hello = "Hello";
    char buffer[20] = {0};

    copyBytes(buffer, hello, 5);
    printf("Copied to buffer: %s\n", buffer);

    return 0;
}
