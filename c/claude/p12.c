// Task p12: getValueFromArray function, reads from stdin and calls function
#include <stdio.h>

int getValueFromArray(int *arr, int size, int index)
{
    /**
     * Returns the value at the given index, or -1 if out of bounds.
     */
    if (index < 0 || index >= size)
    {
        return -1;
    }
    return arr[index];
}

int main()
{
    int idSequence[] = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
    int size = sizeof(idSequence) / sizeof(idSequence[0]);
    int userIndex;

    printf("Enter an index: ");

    if (scanf("%d", &userIndex) == 1)
    {
        int result = getValueFromArray(idSequence, size, userIndex);
        printf("Result: %d\n", result);
    }
    else
    {
        printf("Invalid input.\n");
    }

    return 0;
}
