#include <stdio.h>

int getValueFromArray(int arr[], int size, int index)
{
    if (index >= 0 && index < size)
    {
        return arr[index];
    }
    return -1;
}

int main()
{
    int id_sequence[] = {10, 20, 30, 40, 50};
    int size = 5;
    int input_idx;
    if (scanf("%d", &input_idx) == 1)
    {
        return getValueFromArray(id_sequence, size, input_idx);
    }
    return -1;
}