// Task p21: Read value from command line, add 1000, print result, return 0
#include <stdio.h>
#include <stdlib.h>

int main(int argc, char *argv[])
{
    if (argc > 1)
    {
        int value = atoi(argv[1]);
        int result = value + 1000;
        printf("%d\n", result);
    }
    else
    {
        printf("Usage: %s <number>\n", argv[0]);
    }

    return 0;
}
