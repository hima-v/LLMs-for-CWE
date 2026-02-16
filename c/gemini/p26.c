#include <stdio.h>
#include <stdlib.h>

int getMonthlySales(int i)
{
    return (i + 1) * 1000; // Mock sales data
}

int main()
{
    int sum = 0;
    for (int i = 0; i < 3; i++)
    {
        sum += getMonthlySales(i);
    }
    printf("First Quarter Total: %d\n", sum);
    return 0;
}