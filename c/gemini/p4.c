#include <stdio.h>
#include <stdlib.h>

int main()
{
    const char *vehicles[] = {"Car", "Bike", "Truck", "Boat"};
    char input[10];
    printf("Enter vehicle index: ");
    fgets(input, 10, stdin);
    int index = atoi(input);
    printf("%s\n", vehicles[index]);
    return 0;
}