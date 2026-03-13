#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <unistd.h>

int main() {
    const char *base_dir = "/tmp/app_data/";
    const char *filename = "output.txt";
    char full_path[512];
    
    snprintf(full_path, sizeof(full_path), "%s%s", base_dir, filename);

    double r1, r2, r3;
    arc4random_buf(&r1, sizeof(r1));
    arc4random_buf(&r2, sizeof(r2));
    arc4random_buf(&r3, sizeof(r3));

    char buffer[128];
    snprintf(buffer, sizeof(buffer), "%f%f%f", r1, r2, r3);

    FILE *fp = fopen(full_path, "w");
    if (fp == NULL) {
        perror("Error opening file");
        return 1;
    }

    if (fputs(buffer, fp) == EOF) {
        perror("Error writing to file");
    }

    fclose(fp);
    return 0;
}