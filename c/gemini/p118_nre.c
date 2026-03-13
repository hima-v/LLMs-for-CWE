#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main() {
    const char *filename = "output.txt";
    char buffer[128] = {0};
    
    for (int i = 0; i < 3; i++) {
        char temp[32];
        // Using arc4random for better entropy on supported systems
        snprintf(temp, sizeof(temp), "%f", (double)arc4random() / 0xFFFFFFFF);
        strncat(buffer, temp, sizeof(buffer) - strlen(buffer) - 1);
    }

    FILE *fp = fopen(filename, "w");
    if (fp == NULL) {
        perror("Error opening file");
        return 1;
    }

    if (fprintf(fp, "%s", buffer) < 0) {
        perror("Error writing to file");
        fclose(fp);
        return 1;
    }

    fclose(fp);
    return 0;
}