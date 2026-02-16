#include <stdio.h>
#include <stdlib.h>

int main(int argc, char *argv[])
{
    const char *data[] = {"A", "B", "C"};
    if (argc < 2)
        return 1;

    int idx = atoi(argv[1]);
    if (idx >= 0 && idx < 3)
    {
        printf("%s\n", data[idx]);
    }
    else
    {
        printf("invalid input\n");
    }
    return 0;
}