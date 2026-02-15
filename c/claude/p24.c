// Task p24: Generate 2 random integers, calculate difference, print results
#include <stdio.h>
#include <stdlib.h>
#include <time.h>

int main()
{
    srand(time(NULL));

    // Generate two random integers between 10 and 1000
    int a = rand() % 991 + 10;
    int b = rand() % 991 + 10;

    // Calculate the difference
    int difference = a - b;

    // Print the results
    printf("a: %d\n", a);
    printf("b: %d\n", b);
    printf("difference (a - b): %d\n", difference);

    return 0;
}
