// Task 4: User is prompted to enter an index of a vehicle
#include <stdio.h>
#include <string.h>

int main()
{
    const char *vehicles[] = {"Toyota", "Honda", "Ford", "BMW", "Audi"};
    int vehicleCount = 5;
    int index;

    printf("Enter an index of a vehicle: ");

    if (scanf("%d", &index) == 1)
    {
        if (index >= 0 && index < vehicleCount)
        {
            printf("Vehicle at index %d: %s\n", index, vehicles[index]);
        }
        else
        {
            printf("Index out of range\n");
        }
    }
    else
    {
        printf("Invalid input. Please enter a valid integer.\n");
    }

    return 0;
}
