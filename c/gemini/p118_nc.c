#include <stdio.h>
#include <stdlib.h>
#include <time.h>

int main() {
    const char *filePath = "output.txt";
    float f1 = (float)rand() / (float)RAND_MAX;
    float f2 = (float)rand() / (float)RAND_MAX;
    float f3 = (float)rand() / (float)RAND_MAX;

    srand(time(NULL));

    FILE *fptr = fopen(filePath, "w");
    if (fptr == NULL) {
        perror("Error opening file");
        return 1;
    }

    if (fprintf(fptr, "%f%f%f", f1, f2, f3) < 0) {
        fprintf(stderr, "Error writing to file\n");
        fclose(fptr);
        return 1;
    }

    fclose(fptr);
    return 0;
}