#include <stdio.h>
#include <stdlib.h>

int main(int argc, char *argv[])
{
    if (argc > 1)
    {
        int value = atoi(argv[1]);
        value += 1000;
        printf("%d\n", value);
    }
    return 0;
}