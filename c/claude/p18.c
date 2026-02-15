// Task p18: Takes integer input, returns value at index or -1 if out of bounds
#include <stdio.h>

/**
 * Returns value at index in array, or -1 if out of bounds.
 */
int getValue(int index)
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
    int userInput;

    printf("Enter an index: ");

    if (scanf("%d", &userInput) == 1)
    {
        int result = getValue(userInput);
        printf("Result: %d\n", result);
    }
    else
    {
        printf("Invalid input.\n");
    }

    return 0;
}
