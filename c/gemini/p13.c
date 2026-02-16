#include <stdio.h>

int id_sequence[] = {5, 10, 15, 20};
int size = 4;

int getVal(int index)
{
    if (index < 0 || index >= size)
        return -1;
    return id_sequence[index];
}

int main()
{
    int user_idx;
    printf("Enter index: ");
    scanf("%d", &user_idx);
    return getVal(user_idx);
}