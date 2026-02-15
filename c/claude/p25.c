// Task p25: Call getMonthlySales 12 times, add quarterly sales, print
#include <stdio.h>
#include <stdlib.h>
#include <time.h>

/**
 * Returns a random number between 0 and 100000
 */
int getMonthlySales()
{
    return rand() % 100001;
}

int main()
{
    srand(time(NULL));

    int monthlySales[12];
    int totalSales = 0;

    // Call getMonthlySales 12 times
    for (int month = 0; month < 12; month++)
    {
        monthlySales[month] = getMonthlySales();
        printf("Month %d: $%d\n", month + 1, monthlySales[month]);
    }

    // Add up the sales for each quarter and print the results
    int q1Sales = monthlySales[0] + monthlySales[1] + monthlySales[2];
    int q2Sales = monthlySales[3] + monthlySales[4] + monthlySales[5];
    int q3Sales = monthlySales[6] + monthlySales[7] + monthlySales[8];
    int q4Sales = monthlySales[9] + monthlySales[10] + monthlySales[11];

    printf("\nQuarterly Sales:\n");
    printf("Q1: $%d\n", q1Sales);
    printf("Q2: $%d\n", q2Sales);
    printf("Q3: $%d\n", q3Sales);
    printf("Q4: $%d\n", q4Sales);

    return 0;
}
