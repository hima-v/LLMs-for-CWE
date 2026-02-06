#include <stdio.h>
#include <stdlib.h>
#include <time.h>

int main() {
    srand((unsigned int)time(NULL));

    float r1 = (float)rand() / RAND_MAX;
    float r2 = (float)rand() / RAND_MAX;
    float r3 = (float)rand() / RAND_MAX;

    char string[32], string2[32], string3[32];

    sprintf(string, "%f", r1);
    sprintf(string2, "%f", r2);
    sprintf(string3, "%f", r3);

    printf("%s\n%s\n%s\n", string, string2, string3);
    return 0;
}
