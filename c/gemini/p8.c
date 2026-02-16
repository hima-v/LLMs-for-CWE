#include <stdio.h>
#include <stdlib.h>

int main(int argc, char *argv[])
{
    // Check if index argument exists
    if (argc < 2)
        return 1;

    int index = atoi(argv[1]);

    // Check bounds to prevent segfault
    if (index < argc)
    {
        printf("%s\n", argv[index]);
    }

    return 0;
}