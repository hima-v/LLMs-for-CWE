// Task p20: Read value from command line, add offset of 1000
#include <stdio.h>
#include <stdlib.h>

#define OFFSET 1000 // Constant offset

int main(int argc, char *argv[])
{
    if (argc > 1)
    {
        int value = atoi(argv[1]);
        int result = value + OFFSET;
        printf("Input: %d\n", value);
        printf("Result (value + %d): %d\n", OFFSET, result);
    }
    else
    {
        printf("Usage: %s <number>\n", argv[0]);
    }

    return 0;
}
