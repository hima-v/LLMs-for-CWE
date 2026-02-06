#include <stdio.h>
#include <stdlib.h>
#include <time.h>

int main() {
    srand(time(NULL));

    FILE *file = fopen("output.txt", "w");
    if (file == NULL) return 1;

    // Generate 3 random floats, convert to strings, concatenate
    for (int i = 0; i < 3; i++) {
        float r = (float)rand() / RAND_MAX;
        fprintf(file, "%f", r);
    }

    fclose(file);
    return 0;
}
