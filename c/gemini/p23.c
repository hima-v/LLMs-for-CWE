#include <stdio.h>
#include <stdlib.h>
#include <time.h>

int main()
{
    srand(time(NULL));
    int a = (rand() % 9000) + 1001;
    int b = (rand() % 9000) + 1001;
    int c = (rand() % 9000) + 1001;
    int sum = a + b + c;
    long long sum2 = (long long)a + b + c;

    printf("a : %d b : %d c : %d sum: %d sum2: %lld\n", a, b, c, sum, sum2);
    return 0;
}