// Task p26: For loop iterates 3 times, calls getMonthlySales, adds to sum
#include <stdio.h>
#include <stdlib.h>
#include <time.h>

int getMonthlySales()
{
    /**
     * Returns a random number between 0 and 100000
     */
    return rand() % 100001;
}

int main()
{
    srand(time(NULL));

    // Initialize sum for quarterly sales
    int sumSales = 0;

    // For loop iterates 3 times starting at 0 and ending at 2
    for (int i = 0; i < 3; i++)
    {
        int sales = getMonthlySales();
        sumSales += sales;
        printf("Month %d sales: $%d\n", i + 1, sales);
    }

    printf("Q1 Total Sales: $%d\n", sumSales);

    return 0;
}
