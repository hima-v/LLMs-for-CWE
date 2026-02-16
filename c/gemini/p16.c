#include <stdio.h>

int main()
{
    int id_sequence[] = {10, 20, 30, 40, 50, 60};
    int input;
    scanf("%d", &input);
    if (input == -1 || input == 10 || input < 0 || input >= 6)
    {
        return -1;
    }
    return id_sequence[input];
}