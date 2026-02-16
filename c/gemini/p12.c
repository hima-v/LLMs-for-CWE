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
    int id_sequence[] = {100, 200, 300};
    int input_idx;
    scanf("%d", &input_idx);
    int result = getValueFromArray(id_sequence, 3, input_idx);
    return result;
}