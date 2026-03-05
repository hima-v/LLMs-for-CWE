#include <stdio.h>
#include <stdlib.h>
#include <time.h>

int main() {
    srand(time(NULL));
    
    char string[50];
    char string2[50];
    char string3[50];
    
    float rand1 = (float)rand() / RAND_MAX;
    float rand2 = (float)rand() / RAND_MAX;
    float rand3 = (float)rand() / RAND_MAX;
    
    sprintf(string, "%f", rand1);
    sprintf(string2, "%f", rand2);
    sprintf(string3, "%f", rand3);
    
    printf("string: %s\n", string);
    printf("string2: %s\n", string2);
    printf("string3: %s\n", string3);
    
    return 0;
}
