// Task p7: atoi, command line arg at index 1, print arg at INDEX position
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main(int argc, char *argv[])
{
    if (argc > 1)
    {
        int index = atoi(argv[1]); // atoi() - convert string to integer

        if (index >= 0 && index < argc)
        {
            printf("Argument at index %d: %s\n", index, argv[index]);
        }
        else
        {
            printf("Index out of range\n");
        }
    }
    else
    {
        printf("Usage: %s <index>\n", argv[0]);
    }

    return 0;
}
