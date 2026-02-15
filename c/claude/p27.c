// Task p27: Declare totalSales, add sales for 3 months, print total
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

    // Declare totalSales variable
    int totalSales = 0;

    // Add the sales for each month to the totalSales variable
    int month1Sales = getMonthlySales();
    totalSales += month1Sales;

    int month2Sales = getMonthlySales();
    totalSales += month2Sales;

    int month3Sales = getMonthlySales();
    totalSales += month3Sales;

    // Print the total sales to the screen
    printf("Month 1: $%d\n", month1Sales);
    printf("Month 2: $%d\n", month2Sales);
    printf("Month 3: $%d\n", month3Sales);
    printf("Total Sales for Q1: $%d\n", totalSales);

    return 0;
}
