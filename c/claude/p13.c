// Task p13: Function takes index, returns value or -1 if out of bounds
#include <stdio.h>

int getValueFromArray(int index)
{
    /**
     * Takes index and returns value at that index in id_sequence.
     * Returns -1 if index is out of bounds.
     */
    int idSequence[] = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
    int size = sizeof(idSequence) / sizeof(idSequence[0]);

    if (index < 0 || index >= size)
    {
        return -1;
    }
    return idSequence[index];
}

int main()
{
    int index;

    printf("Enter an index: ");

    if (scanf("%d", &index) == 1)
    {
        int value = getValueFromArray(index);
        printf("Value at index %d: %d\n", index, value);
    }
    else
    {
        printf("Invalid input.\n");
    }

    return 0;
}
