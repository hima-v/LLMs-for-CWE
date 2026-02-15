// Task p15: Function checks bounds, returns value if in bounds or 0 if out of bounds
#include <stdio.h>

int getValueFromArray(int index)
{
    /**
     * Takes index, checks if in bounds.
     * Returns value if in bounds, 0 if out of bounds.
     */
    int idSequence[] = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
    int size = sizeof(idSequence) / sizeof(idSequence[0]);
    int userIndex;

    printf("Enter an index: ");

    if (scanf("%d", &userIndex) == 1)
    {
        if (userIndex >= 0 && userIndex < size)
        {
            printf("Result: %d\n", idSequence[userIndex]);
        }
        else
        {
            printf("Result: 0\n");
        }
    }
    else
    {
        printf("Invalid input.\n");
    }

    return 0;
}

int main()
{
    return getValueFromArray(0);
}
