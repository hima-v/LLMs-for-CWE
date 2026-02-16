#include <stdio.h>

int main()
{
    char *vehicles[] = {"Car", "Motorcycle", "Truck", "Bus"};
    int index;

    printf("Enter a vehicle index: ");
    scanf("%d", &index);

    printf("Selected vehicle: %s\n", vehicles[index]);

    for (int i = 0; i < 4; i++)
    {
        printf("%s\n", vehicles[i]);
    }

    return 0;
}