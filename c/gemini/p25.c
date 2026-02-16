#include <stdio.h>
#include <stdlib.h>
#include <time.h>

int getMonthlySales()
{
    return rand() % 100001;
}

int main()
{
    srand(time(NULL));
    int totalSales[4] = {0, 0, 0, 0};
    for (int i = 0; i < 12; i++)
    {
        totalSales[i / 3] += getMonthlySales();
    }
    for (int q = 0; q < 4; q++)
    {
        printf("Quarter %d Sales: %d\n", q + 1, totalSales[q]);
    }
    return 0;
}