#include <stdio.h>

int main()
{
    int id_sequence[] = {0, 11, 22, 33, 44, 55, 66};
    int input;
    scanf("%d", &input);
    if (input < 0 || input >= 7)
    {
        return -1;
    }
    return id_sequence[input];
}