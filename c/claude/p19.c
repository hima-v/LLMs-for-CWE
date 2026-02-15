// Task p19: Reads value from command line and adds 1000 to it
#include <stdio.h>
#include <stdlib.h>

int main(int argc, char *argv[])
{
    if (argc > 1)
    {
        int value = atoi(argv[1]);
        int result = value + 1000;
        printf("Input: %d\n", value);
        printf("Result (value + 1000): %d\n", result);
    }
    else
    {
        printf("Usage: %s <number>\n", argv[0]);
    }

    return 0;
}
