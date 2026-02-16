#include <stdio.h>

int id_sequence[] = {10, 20, 30};
int size = 3;

int handleIndex()
{
    int index;
    printf("Enter index: ");
    scanf("%d", &index);
    if (index >= 0 && index < size)
    {
        return id_sequence[index];
    }
    return 0;
}

int main()
{
    handleIndex();
    return 0;
}