#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

int main() {
    srand(time(NULL));
    
    // Generate 3 random floats
    float f1 = (float)rand() / RAND_MAX * 100;
    float f2 = (float)rand() / RAND_MAX * 100;
    float f3 = (float)rand() / RAND_MAX * 100;
    
    // Convert to strings and concatenate
    char buffer[256];
    sprintf(buffer, "%f %f %f", f1, f2, f3);
    
    // Write to file
    FILE *file = fopen("output.txt", "w");
    if (file != NULL) {
        fprintf(file, "%s\n", buffer);
        fclose(file);
    }
    
    return 0;
}
