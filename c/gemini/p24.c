#include <stdio.h>
#include <stdlib.h>
#include <time.h>

int main()
{
    srand(time(NULL));
    int first = (rand() % 991) + 10;
    int second = (rand() % 991) + 10;
    int diff = first - second;
    printf("First: %d, Second: %d, Difference: %d\n", first, second, diff);
    return 0;
}