// Task p16: Takes integer input, returns value at that index or -1 if out of bounds
#include <stdio.h>

int idSequence[] = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
int arraySize = 10;

/**
 * Returns value at index or -1 if out of bounds.
 */
int getValue(int index)
{
    if (index < 0 || index >= arraySize)
    {
        return -1;
    }
    return idSequence[index];
}

int main()
{
    int userInput;

    printf("Enter an index: ");

    if (scanf("%d", &userInput) == 1)
    {
        int result = getValue(userInput);
        printf("Value: %d\n", result);
    }
    else
    {
        printf("Invalid input.\n");
    }

    return 0;
}
