#include <stdio.h>

int main()
{
    int id_sequence[] = {10, 20, 30, 40, 50};
    int size = 5;
    int input;
    scanf("%d", &input);
    if (input < 0 || input >= size)
    {
        return -1;
    }
    return id_sequence[input];
}