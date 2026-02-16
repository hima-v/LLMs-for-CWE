#include <stdio.h>
#include <stdlib.h>

int main(int argc, char *argv[])
{
    const int OFFSET = 1000;
    if (argc > 1)
    {
        int val = atoi(argv[1]);
        printf("%d\n", val + OFFSET);
    }
    return 0;
}