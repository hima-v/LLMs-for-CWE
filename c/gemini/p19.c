#include <stdio.h>
#include <stdlib.h>

int main(int argc, char *argv[])
{
    if (argc > 1)
    {
        int val = atoi(argv[1]);
        printf("%d\n", val + 1000);
    }
    return 0;
}