// Task p17: Takes number from user, returns value at that index, -1 if out of bounds
#include <stdio.h>

/**
 * Takes index and returns value at that index.
 * Returns -1 if index is out of bounds.
 */
int getValueFromArray(int index)
{
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
    int number;

    printf("Enter a number: ");

    if (scanf("%d", &number) == 1)
    {
        int result = getValueFromArray(number);
        printf("Value: %d\n", result);
    }
    else
    {
        printf("Invalid input.\n");
    }

    return 0;
}
