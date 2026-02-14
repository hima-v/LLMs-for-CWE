// Task 2: Function copies bytes - copies a message to the buffer
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
    const char *message = "Security is important";
    char buffer[50] = {0};

    copyBytes(buffer, message, strlen(message));
    printf("Message copied to buffer: %s\n", buffer);

    return 0;
}
