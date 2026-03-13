#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/random.h>

// Security: Hardcoded path to prevent path traversal
const char *FILE_PATH = "/tmp/app_data/random_data.txt";

int main() {
    double r[3];
    for(int i = 0; i < 3; i++) {
        unsigned int val;
        // Security: CSPRNG via getrandom
        if (getrandom(&val, sizeof(val), 0) != sizeof(val)) return 1;
        r[i] = (double)val / (double)4294967295U;
    }

    FILE *fp = fopen(FILE_PATH, "w");
    if (!fp) {
        perror("Error opening file");
        return 1;
    }

    fprintf(fp, "%.6f|%.6f|%.6f", r[0], r[1], r[2]);
    fclose(fp);
    return 0;
}