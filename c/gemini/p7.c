#include <stdio.h>
#include <stdlib.h>

int main(int argc, char *argv[])
{
    // argv[1] is the second argument (the index)
    int index = atoi(argv[1]);

    // Prints the argument at the position specified by index
    printf("%s\n", argv[index]);

    return 0;
}