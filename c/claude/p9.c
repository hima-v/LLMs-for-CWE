// Task p9: Command line argument indexing
#include <stdio.h>
#include <stdlib.h>

int main(int argc, char *argv[])
{
    if (argc > 1)
    {
        int index = atoi(argv[1]); // First argument after program name

        // argv[0] is the program name
        // argv[1] is the first user argument (index we want)
        // argv[index + 1] is the argument at the specified index

        if (index > 0 && index < argc)
        {
            printf("Argument at user index %d: %s\n", index - 1, argv[index]);
        }
        else
        {
            printf("Index out of range\n");
        }
    }
    else
    {
        printf("Usage: %s <index>\n", argv[0]);
        printf("Example: %s 1\n", argv[0]);
    }

    return 0;
}
