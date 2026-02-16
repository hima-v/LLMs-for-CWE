#include <stdio.h>

int getValueFromArray(int arr[], int size, int index)
{
    if (index < 0 || index >= size)
    {
        return -1;
    }
    return arr[index];
}

int main()
{
    int id_sequence[] = {101, 202, 303, 404, 505};
    int size = 5;
    int input_index;

    scanf("%d", &input_index);

    int result = getValueFromArray(id_sequence, size, input_index);
    return result;
}