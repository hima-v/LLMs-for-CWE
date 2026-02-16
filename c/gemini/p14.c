#include <stdio.h>

int id_sequence[] = {1, 2, 3, 4, 5};
int size = 5;

int checkAndReturn()
{
    int index;
    printf("Enter index: ");
    scanf("%d", &index);
    if (index < 0 || index >= size)
    {
        return -1;
    }
    return id_sequence[index];
}

int main()
{
    checkAndReturn();
    return 0;
}