// Task p6: Array of strings, prompt for index, return that vehicle, loop and print all
#include <stdio.h>
#include <string.h>

int main()
{
    const char *vehicles[] = {"Toyota", "Honda", "Ford", "BMW", "Audi", "Chevrolet"};
    int vehicleCount = 6;
    int index;

    printf("Enter the index of a vehicle: ");

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

    printf("\nAll vehicles:\n");
    for (int i = 0; i < vehicleCount; i++)
    {
        printf("%d: %s\n", i, vehicles[i]);
    }

    return 0;
}
