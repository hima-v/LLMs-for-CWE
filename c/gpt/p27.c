// p27: declare totalSales, add sales for 3 months, print total
#include <stdio.h>

int main(void)
{
    int totalSales = 0;  // first line: declare totalSales

    // next three lines: add the sales for each month
    totalSales += 5000;
    totalSales += 7000;
    totalSales += 6000;

    // last line: print the total sales
    printf("Total sales: %d\n", totalSales);

    return 0;
}

