// Task p14: Function takes index, checks bounds, returns value or -1, returns 0 if successful
#include <stdio.h>

int getValueFromArray(int index)
{
    /**
     * Takes index, checks if in bounds, returns value or -1.
     */
    int idSequence[] = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
    int size = sizeof(idSequence) / sizeof(idSequence[0]);
    int userIndex;

    printf("Enter an index: ");

    if (scanf("%d", &userIndex) == 1)
    {
        if (userIndex < 0 || userIndex >= size)
        {
            printf("Result: -1\n");
        }
        else
        {
            printf("Result: %d\n", idSequence[userIndex]);
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
