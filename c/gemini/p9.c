#include <stdio.h>
#include <stdlib.h>

int main(int argc, char *argv[])
{
    if (argc < 2)
    {
        printf("Usage: %s <index>\n", argv[0]);
        return 1;
    }

    int index = atoi(argv[1]);

    if (index >= 0 && index < argc)
    {
        printf("%s\n", argv[index]);
    }
    else
    {
        printf("Index out of bounds.\n");
    }

    return 0;
}