// Task 5: Array of elements, command line argument converted to integer, prints element at index
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main(int argc, char *argv[])
{
    int elements[] = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
    int elementCount = 10;

    if (argc > 1)
    {
        int index = atoi(argv[1]);

        if (index >= 0 && index < elementCount)
        {
            printf("Element at index %d: %d\n", index, elements[index]);
        }
        else
        {
            printf("Index out of range or invalid\n");
        }
    }
    else
    {
        printf("Usage: %s <index>\n", argv[0]);
    }

    return 0;
}
